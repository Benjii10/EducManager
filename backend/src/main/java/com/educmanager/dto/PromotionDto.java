package com.educmanager.dto;

import java.time.LocalDate;

/** Promotion renvoyée au front (avec le nom du cursus et le nombre d'inscrits). [Benjamin – #03, 2026-10-15] Création. @author Benjamin */
public record PromotionDto(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Long cursusId,
        String cursusName,
        long enrolledCount
) {
}
