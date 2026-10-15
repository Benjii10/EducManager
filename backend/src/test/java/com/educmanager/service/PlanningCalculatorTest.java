package com.educmanager.service;

import com.educmanager.dto.PlanningItem;
import com.educmanager.entity.Course;
import com.educmanager.entity.CursusCourse;
import com.educmanager.entity.Promotion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests du calcul de planning (jours ouvrés). [Benjamin – #06, 2026-10-15] Création. @author Benjamin */
class PlanningCalculatorTest {

    private static CursusCourse cc(int ordre, String code, Integer jours) {
        return CursusCourse.builder().ordre(ordre)
                .course(Course.builder().code(code).titre("Cours " + code).dureeJours(jours).build())
                .build();
    }

    @Test
    void enchaineLesCoursEnSautantLesWeekEnds() {
        // lundi 7 septembre 2026
        Promotion promo = Promotion.builder().name("P").startDate(LocalDate.of(2026, 9, 7)).build();

        List<PlanningItem> items = PlanningCalculator.compute(promo, List.of(cc(1, "A", 5), cc(2, "B", 3)));

        assertThat(items).hasSize(2);
        assertThat(items.get(0).startDate()).isEqualTo(LocalDate.of(2026, 9, 7));   // lundi
        assertThat(items.get(0).endDate()).isEqualTo(LocalDate.of(2026, 9, 11));    // vendredi
        assertThat(items.get(1).startDate()).isEqualTo(LocalDate.of(2026, 9, 14));  // lundi suivant
        assertThat(items.get(1).endDate()).isEqualTo(LocalDate.of(2026, 9, 16));    // mercredi
    }

    @Test
    void uneDateDeDebutLeSamediCommenceLeLundi() {
        Promotion promo = Promotion.builder().name("P").startDate(LocalDate.of(2026, 9, 5)).build(); // samedi

        List<PlanningItem> items = PlanningCalculator.compute(promo, List.of(cc(1, "A", 1)));

        assertThat(items.get(0).startDate()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(items.get(0).endDate()).isEqualTo(LocalDate.of(2026, 9, 7));
    }

    @Test
    void uneDureeNulleOuAbsenteCompteUnJour() {
        Promotion promo = Promotion.builder().name("P").startDate(LocalDate.of(2026, 9, 7)).build();

        List<PlanningItem> items = PlanningCalculator.compute(promo, List.of(cc(1, "A", null), cc(2, "B", 0)));

        assertThat(items.get(0).endDate()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(items.get(1).startDate()).isEqualTo(LocalDate.of(2026, 9, 8));
    }

    @Test
    void ajoutDeJoursOuvres() {
        assertThat(PlanningCalculator.addWorkingDays(LocalDate.of(2026, 9, 11), 1))  // vendredi + 1
                .isEqualTo(LocalDate.of(2026, 9, 14));                                 // lundi
        assertThat(PlanningCalculator.addWorkingDays(LocalDate.of(2026, 9, 7), 0))
                .isEqualTo(LocalDate.of(2026, 9, 7));
    }
}
