package com.educmanager.dto;

import com.educmanager.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Modification d'un compte ; password vide/null = mot de passe inchangé. [Benjamin – #03, 2026-10-15] Création. @author Benjamin */
public record UpdateUserRequest(
        @NotBlank(message = "Le prénom est obligatoire") @Size(max = 60) String firstName,
        @NotBlank(message = "Le nom est obligatoire") @Size(max = 60) String lastName,
        @NotNull(message = "Le rôle est obligatoire") Role role,
        @Pattern(regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                message = "Le mot de passe doit faire 8 à 72 caractères avec au moins une lettre et un chiffre")
        String password
) {
}
