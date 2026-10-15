package com.educmanager.dto;

import com.educmanager.enums.Role;

/** Compte utilisateur renvoyé au front (jamais le hash du mot de passe). [Benjamin – #03, 2026-10-15] Création. @author Benjamin */
public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role,
        Long studentId
) {
}
