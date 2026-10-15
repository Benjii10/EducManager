package com.educmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

/**
 * État de santé de l'application, utilisé par le healthcheck Docker et la supervision.
 * Public (aucune donnée sensible) : renvoie 200 {"status":"UP"} si la base relationnelle répond,
 * 503 {"status":"DOWN"} sinon.
 *
 * <p>[Benjamin – #08, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                return ResponseEntity.ok(Map.of("status", "UP"));
            }
        } catch (Exception e) {
            // base injoignable : on renvoie DOWN sans détail technique
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("status", "DOWN"));
    }
}
