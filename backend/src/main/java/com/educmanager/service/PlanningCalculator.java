package com.educmanager.service;

import com.educmanager.dto.PlanningItem;
import com.educmanager.entity.CursusCourse;
import com.educmanager.entity.Promotion;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Calcule le planning prévisionnel d'une promotion : les cours du cursus s'enchaînent
 * dans l'ordre défini (CursusCourse.ordre), chacun durant {@code dureeJours} jours ouvrés
 * (lundi → vendredi), à partir de la date de début de la promotion.
 *
 * <p>Classe sans dépendance (fonction pure) pour être testée unitairement facilement.</p>
 *
 * <p>[Benjamin – #03, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
public final class PlanningCalculator {

    private PlanningCalculator() {
    }

    public static List<PlanningItem> compute(Promotion promotion, List<CursusCourse> orderedCourses) {
        List<PlanningItem> items = new ArrayList<>();
        LocalDate cursor = nextWorkingDay(promotion.getStartDate());

        for (CursusCourse cc : orderedCourses) {
            int days = cc.getCourse().getDureeJours() == null ? 1 : Math.max(1, cc.getCourse().getDureeJours());
            LocalDate start = cursor;
            LocalDate end = addWorkingDays(start, days - 1);
            items.add(new PlanningItem(promotion.getName(), cc.getOrdre(),
                    cc.getCourse().getCode(), cc.getCourse().getTitre(), start, end));
            cursor = nextWorkingDay(end.plusDays(1));
        }
        return items;
    }

    /** Renvoie la date elle-même si c'est un jour ouvré, sinon le lundi suivant. */
    static LocalDate nextWorkingDay(LocalDate date) {
        LocalDate d = date;
        while (isWeekend(d)) {
            d = d.plusDays(1);
        }
        return d;
    }

    /** Ajoute n jours ouvrés (n = 0 → la date elle-même). */
    static LocalDate addWorkingDays(LocalDate start, int n) {
        LocalDate d = start;
        int added = 0;
        while (added < n) {
            d = d.plusDays(1);
            if (!isWeekend(d)) {
                added++;
            }
        }
        return d;
    }

    private static boolean isWeekend(LocalDate d) {
        return d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
