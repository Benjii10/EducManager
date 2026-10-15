package com.educmanager.dto;

import java.time.LocalDate;

/**
 * Une ligne du planning d'un élève : un cours avec ses dates calculées.
 * startDate / endDate sont null pour un cours à l'unité pas encore planifié.
 * [Benjamin – #03, 2026-10-15] Création. @author Benjamin
 */
public record PlanningItem(
        String promotionName,
        Integer ordre,
        String courseCode,
        String courseTitle,
        LocalDate startDate,
        LocalDate endDate
) {
}
