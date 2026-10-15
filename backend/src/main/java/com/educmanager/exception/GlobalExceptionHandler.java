package com.educmanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(
            BadRequestException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    // [Benjamin – #01, 2026-10-15] Un @PreAuthorize refusé lève AccessDeniedException :
    // sans ce handler, elle tombait dans handleException() et renvoyait 500 au lieu de 403.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildErrorResponse(
                        HttpStatus.FORBIDDEN,
                        "Accès refusé : droits insuffisants pour cette action.",
                        request.getRequestURI()
                ));
    }

    // [Benjamin – #03, 2026-10-15] Erreurs de validation (@Valid) → 400 avec le détail par champ
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = buildErrorResponse(HttpStatus.BAD_REQUEST,
                "Données invalides", request.getRequestURI());
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    // [Benjamin – #03] JSON illisible ou valeur d'enum inconnue → 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest()
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, "Corps de requête invalide", request.getRequestURI()));
    }

    // [Benjamin – #03] Règles métier de CursusCourseService (IllegalArgumentException) → 400 au lieu de 500
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest()
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI()));
    }

    // [Benjamin – #03] Doublon ou ressource encore utilisée → 409
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(
            ConflictException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI()));
    }

    // [Benjamin – #03] Contrainte SQL violée (clé étrangère, unicité) → 409 sans exposer le détail SQL
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(HttpStatus.CONFLICT,
                        "Opération impossible : la donnée est liée à d'autres éléments ou existe déjà.",
                        request.getRequestURI()));
    }

    // [Benjamin – #04] Base de données (ex. MongoDB du journal d'audit) injoignable → 503
    @ExceptionHandler(org.springframework.dao.DataAccessResourceFailureException.class)
    public ResponseEntity<Map<String, Object>> handleDataSourceDown(
            Exception exception,
            HttpServletRequest request
    ) {
        log.warn("Source de données indisponible sur {} : {}", request.getRequestURI(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE,
                        "Service temporairement indisponible (base de données injoignable)", request.getRequestURI()));
    }

    // [Benjamin – #03] Paramètre d'URL du mauvais type (ex. /api/courses/abc) → 400
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            Exception exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest()
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, "Paramètre invalide", request.getRequestURI()));
    }

    // [Benjamin – #03] Anti force brute du login → 429
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyRequests(
            TooManyRequestsException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage(), request.getRequestURI()));
    }

    // [Benjamin – #03] Erreur inattendue : on journalise le détail côté serveur mais on ne renvoie
    // qu'un message générique (OWASP : ne pas exposer de détails techniques au client).
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception exception,
            HttpServletRequest request
    ) {
        // Exceptions standard de Spring MVC (404 route inconnue, 405 méthode non supportée,
        // paramètre manquant...) : on garde leur code HTTP d'origine.
        if (exception instanceof org.springframework.web.ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            String message = status == HttpStatus.NOT_FOUND ? "Ressource introuvable" : status.getReasonPhrase();
            return ResponseEntity.status(status).body(buildErrorResponse(status, message, request.getRequestURI()));
        }
        log.error("Erreur inattendue sur {}", request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erreur interne du serveur",
                        request.getRequestURI()
                ));
    }

    private Map<String, Object> buildErrorResponse(HttpStatus status, String message, String path) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }
}
