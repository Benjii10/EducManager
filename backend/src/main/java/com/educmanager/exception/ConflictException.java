package com.educmanager.exception;

/**
 * Conflit avec l'état actuel des données (doublon, suppression impossible car la ressource est utilisée...).
 * Traduit en HTTP 409 par {@link GlobalExceptionHandler}.
 *
 * <p>[Benjamin – #03, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
