package com.educmanager.dto;

import com.educmanager.enums.EnrollmentType;

import java.time.LocalDate;

/** Inscription renvoyée au front, avec les libellés utiles à l'affichage. [Benjamin – #03, 2026-10-15] Création. @author Benjamin */
public record EnrollmentResponse(
        Long id,
        Long studentId,
        String studentName,
        EnrollmentType type,
        Long promotionId,
        String promotionName,
        Long courseId,
        String courseTitle,
        LocalDate enrollmentDate,
        boolean forced
) {
}
