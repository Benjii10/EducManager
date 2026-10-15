package com.educmanager.entity;

import com.educmanager.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "user_account")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // [Benjamin – #01] contient désormais un hash BCrypt (60 caractères), jamais le mot de passe en clair
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // [Benjamin – #01] nom affiché dans l'interface (sidebar, tableau de bord)
    private String firstName;

    private String lastName;
}