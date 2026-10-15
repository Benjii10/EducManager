package com.educmanager.dto;

import com.educmanager.enums.EnrollmentType;
import jakarta.validation.constraints.NotNull;

/**
 * Demande d'inscription. type = PROMOTION → promotionId obligatoire ; type = UNIT → courseId obligatoire.
 * forced = true permet à la référente d'inscrire dans une promotion déjà terminée.
 * [Benjamin – #03, 2026-10-15] Création. (remplace EnrollmentDTO) @author Benjamin
 */
public record EnrollmentRequest(
        @NotNull(message = "L'élève est obligatoire") Long studentId,
        @NotNull(message = "Le type d'inscription est obligatoire") EnrollmentType type,
        Long promotionId,
        Long courseId,
        Boolean forced
) {
    /** Champ facultatif dans le JSON : absent = false (Jackson 3 refuse null pour un boolean primitif). */
    public boolean isForced() {
        return Boolean.TRUE.equals(forced);
    }
}
