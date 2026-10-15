package com.educmanager.exception;

/**
 * Trop de tentatives (protection anti force brute du login). Traduit en HTTP 429.
 *
 * <p>[Benjamin – #03, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
