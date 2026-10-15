package com.educmanager.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Enregistre automatiquement dans le journal d'audit chaque requête d'écriture
 * (POST / PUT / PATCH / DELETE) sur l'API, avec l'utilisateur, la ressource et le code HTTP.
 * Évite d'ajouter du code d'audit dans chaque contrôleur.
 * Le login est audité à part, dans AuthController (succès / échec / blocage).
 *
 * <p>[Benjamin – #04, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@Component
public class AuditInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    // ObjectProvider : l'intercepteur reste utilisable dans les tests @WebMvcTest
    // (où AuditService n'existe pas) ; dans ce cas, rien n'est audité.
    private final ObjectProvider<AuditService> auditService;

    public AuditInterceptor(ObjectProvider<AuditService> auditService) {
        this.auditService = auditService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String path = request.getRequestURI();
        if (!WRITE_METHODS.contains(request.getMethod()) || !path.startsWith("/api/")
                || path.startsWith("/api/auth/") || path.endsWith("/can-access")) {
            return;
        }
        AuditService service = auditService.getIfAvailable();
        if (service == null) {
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String[] parts = path.substring("/api/".length()).split("/");

        service.record(AuditLog.builder()
                .username(auth != null && auth.isAuthenticated() ? auth.getName() : "anonyme")
                .action(actionOf(request.getMethod()))
                .resource(parts[0])
                .resourceId(parts.length > 1 ? parts[1] : null)
                .httpMethod(request.getMethod())
                .path(path)
                .status(response.getStatus())
                .ipAddress(clientIp(request))
                .build());
    }

    static String actionOf(String method) {
        return switch (method) {
            case "POST" -> "CREATE";
            case "DELETE" -> "DELETE";
            default -> "UPDATE";
        };
    }

    /** IP du client (derrière le reverse proxy nginx, on lit X-Forwarded-For). */
    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
