package com.educmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Clock;

/**
 * Beans transverses.
 * <ul>
 *   <li>{@link Clock} : l'heure courante est injectée (et non lue directement) pour pouvoir
 *       la figer dans les tests (dates d'inscription, blocage du login) ;</li>
 *   <li>{@code @EnableAsync} : le journal d'audit MongoDB est écrit en tâche de fond
 *       pour ne jamais ralentir une requête.</li>
 * </ul>
 *
 * <p>[Benjamin – #03, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@Configuration
@EnableAsync
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
