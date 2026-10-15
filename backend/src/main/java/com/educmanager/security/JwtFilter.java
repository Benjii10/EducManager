package com.educmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Lit l'en-tête {@code Authorization: Bearer <token>} et, si le token est valide,
 * place l'utilisateur et son rôle ({@code ROLE_xxx}) dans le contexte Spring Security.
 *
 * <p>[Benjamin – #01, 2026-10-15] N'est plus un {@code @Component} : il est instancié et branché
 * explicitement dans {@link SecurityConfig}. Sinon Spring Boot l'enregistrait aussi comme filtre
 * servlet classique, exécuté APRÈS la vérification des droits.</p>
 *
 * <p>[Benjamin – #03, 2026-10-15] Le compte est relu en base à chaque requête : un compte supprimé
 * perd immédiatement l'accès et un changement de rôle s'applique sans attendre l'expiration du token.</p>
 */
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtils.validateToken(token)) {
                try {
                    UserDetails user = userDetailsService.loadUserByUsername(jwtUtils.getUsernameFromToken(token));
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (UsernameNotFoundException e) {
                    // compte supprimé depuis l'émission du token : requête traitée comme anonyme (→ 401)
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
