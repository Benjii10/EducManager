package com.educmanager.controller;

import com.educmanager.audit.AuditInterceptor;
import com.educmanager.audit.AuditLog;
import com.educmanager.audit.AuditService;
import com.educmanager.dto.LoginRequest;
import com.educmanager.dto.LoginResponse;
import com.educmanager.entity.UserAccount;
import com.educmanager.exception.TooManyRequestsException;
import com.educmanager.repository.UserAccountRepository;
import com.educmanager.security.JwtUtils;
import com.educmanager.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentification : échange identifiants → token JWT.
 *
 * <p>[Benjamin – #01, 2026-10-15] Vérification du mot de passe avec BCrypt
 * (avant : comparaison en clair), message d'erreur générique (on ne révèle pas si
 * c'est l'email ou le mot de passe qui est faux), et {@code /me} renvoie vraiment
 * l'utilisateur connecté.</p>
 *
 * <p>[Benjamin – #04, 2026-10-15] Blocage après 5 échecs ({@link LoginAttemptService}, HTTP 429)
 * et journalisation des connexions réussies / échouées / bloquées dans MongoDB.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountRepository userAccountRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final AuditService auditService;

    public AuthController(UserAccountRepository userAccountRepository,
                          JwtUtils jwtUtils,
                          PasswordEncoder passwordEncoder,
                          LoginAttemptService loginAttemptService,
                          AuditService auditService) {
        this.userAccountRepository = userAccountRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest http) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return unauthorized();
        }
        String username = request.getUsername().trim().toLowerCase();

        try {
            loginAttemptService.checkNotBlocked(username);
        } catch (TooManyRequestsException e) {
            audit("LOGIN_BLOCKED", username, 429, http);
            throw e;
        }

        UserAccount user = userAccountRepository.findByUsername(username).orElse(null);

        // Même réponse si l'utilisateur n'existe pas ou si le mot de passe est faux
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptService.loginFailed(username);
            audit("LOGIN_FAILURE", username, 401, http);
            return unauthorized();
        }

        loginAttemptService.loginSucceeded(username);
        audit("LOGIN_SUCCESS", username, 200, http);
        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(toResponse(user, token));
    }

    /** Renvoie l'utilisateur correspondant au token envoyé (utile au front pour vérifier une session). */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        return userAccountRepository.findByUsername(authentication.getName())
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(toResponse(user, null)))
                .orElseGet(this::unauthorized);
    }

    private void audit(String action, String username, int status, HttpServletRequest http) {
        auditService.record(AuditLog.builder()
                .action(action)
                .username(username)
                .resource("auth")
                .httpMethod("POST")
                .path("/api/auth/login")
                .status(status)
                .ipAddress(AuditInterceptor.clientIp(http))
                .build());
    }

    private LoginResponse toResponse(UserAccount user, String token) {
        return new LoginResponse(token, user.getRole().name(), user.getId(),
                user.getUsername(), user.getFirstName(), user.getLastName());
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Email ou mot de passe incorrect."));
    }
}
