package com.educmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse de {@code POST /api/auth/login} (et de {@code GET /api/auth/me}, sans token).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private Long userId;
    // [Benjamin – #01] infos d'affichage renvoyées au front après connexion
    private String username;
    private String firstName;
    private String lastName;
}