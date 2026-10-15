package com.educmanager.entity;

import com.educmanager.enums.EnrollmentType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Inscription d'un élève : soit à une promotion complète (type PROMOTION),
 * soit à un cours à l'unité (type UNIT).
 *
 * <p>[Benjamin – #03, 2026-10-15] Remplacement des identifiants « nus » {@code promotionId} et
 * {@code promotionCourseScheduleId} (aucune table derrière) par de vraies relations
 * {@link Promotion} et {@link Course} avec clés étrangères. Les contraintes d'unicité
 * empêchent d'inscrire deux fois le même élève à la même promotion / au même cours.</p>
 */
@Entity
@Data
@NoArgsConstructor
@Table(
        name = "enrollment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "promotion_id"}),
                @UniqueConstraint(columnNames = {"student_id", "course_id"})
        }
)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Inscription acceptée par la référente malgré une règle métier (promotion déjà terminée). */
    @Column(nullable = false)
    private boolean forced;

    @Column(nullable = false)
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentType type;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /** Renseigné si type = PROMOTION. */
    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    /** Renseigné si type = UNIT. */
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}
