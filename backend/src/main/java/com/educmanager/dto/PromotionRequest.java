package com.educmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Données envoyées pour créer / modifier une promotion. [Benjamin – #03, 2026-10-15] Création. @author Benjamin */
public record PromotionRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom fait au plus 100 caractères")
        String name,
        @NotNull(message = "La date de début est obligatoire") LocalDate startDate,
        @NotNull(message = "La date de fin est obligatoire") LocalDate endDate,
        @NotNull(message = "Le cursus est obligatoire") Long cursusId
) {
}
