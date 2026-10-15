package com.educmanager.audit;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

/**
 * Accès aux données MongoDB du journal d'audit (Spring Data MongoDB).
 * Les recherches multicritères paginées passent par {@code MongoTemplate} dans {@link AuditService}.
 *
 * <p>[Benjamin – #04, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);

    long countByActionAndTimestampAfter(String action, Instant after);
}
