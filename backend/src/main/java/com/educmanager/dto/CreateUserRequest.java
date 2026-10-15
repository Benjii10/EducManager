package com.educmanager.dto;

import com.educmanager.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Création d'un compte par l'administrateur. [Benjamin – #03, 2026-10-15] Création. @author Benjamin */
public record CreateUserRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        @Size(max = 120)
        String email,
        @NotBlank(message = "Le prénom est obligatoire") @Size(max = 60) String firstName,
        @NotBlank(message = "Le nom est obligatoire") @Size(max = 60) String lastName,
        @NotNull(message = "Le rôle est obligatoire") Role role,
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le mot de passe doit faire entre 8 et 72 caractères")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Le mot de passe doit contenir au moins une lettre et un chiffre")
        String password
) {
}
