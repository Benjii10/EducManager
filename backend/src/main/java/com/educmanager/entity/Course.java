package com.educmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Benjamin – #03, 2026-10-15] contraintes de validation (API) + unicité du code (base)
    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 30, message = "Le code fait au plus 30 caractères")
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150, message = "Le titre fait au plus 150 caractères")
    @Column(nullable = false, length = 150)
    private String titre;

    @Size(max = 1000, message = "La description fait au plus 1000 caractères")
    @Column(length = 1000)
    private String description;

    @Min(value = 1, message = "La durée est d'au moins 1 jour")
    @Max(value = 200, message = "La durée est d'au plus 200 jours")
    private Integer dureeJours;
}
