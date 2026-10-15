package com.educmanager.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Événement du journal d'audit, stocké dans MongoDB (collection {@code audit_logs}).
 *
 * <p>Pourquoi NoSQL ici : un journal est une suite d'événements indépendants, écrits souvent,
 * jamais modifiés, au format qui peut évoluer ; pas de jointure avec le modèle relationnel.
 * MongoDB permet aussi une expiration automatique (index TTL, voir {@link AuditIndexInitializer}) :
 * les événements sont supprimés après {@code app.audit.retention-days} jours (RGPD : durée de
 * conservation limitée des adresses IP).</p>
 *
 * <p>[Benjamin – #04, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@Document(collection = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id;

    private Instant timestamp;

    /** Identifiant de l'utilisateur (email) ou « anonyme ». */
    private String username;

    /** LOGIN_SUCCESS, LOGIN_FAILURE, LOGIN_BLOCKED, CREATE, UPDATE, DELETE. */
    private String action;

    /** Ressource concernée (filieres, cursus, courses, promotions, enrollment, users...). */
    private String resource;

    private String resourceId;

    private String httpMethod;

    private String path;

    /** Code HTTP de la réponse (permet de repérer les tentatives refusées : 401/403). */
    private int status;

    private String ipAddress;
}
