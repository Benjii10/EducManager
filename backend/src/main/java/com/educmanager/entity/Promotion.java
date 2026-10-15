package com.educmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Promotion = une session d'un cursus avec des dates (ex. « CDA 2026-2027 »).
 * Les élèves s'inscrivent à une promotion ({@link Enrollment} de type PROMOTION).
 *
 * <p>[Benjamin – #03, 2026-10-15] Création (avant, {@code Enrollment} stockait un simple {@code promotionId}
 * sans table ni clé étrangère).</p>
 *
 * @author Benjamin
 */
@Entity
@Table(name = "promotion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cursus_id", nullable = false)
    private Cursus cursus;
}
