package com.educmanager.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Crée au démarrage les index MongoDB du journal d'audit :
 * <ul>
 *   <li>index TTL sur {@code timestamp} : MongoDB supprime seul les événements plus vieux que
 *       {@code app.audit.retention-days} (RGPD : limitation de la durée de conservation) ;</li>
 *   <li>index sur {@code username} pour les recherches.</li>
 * </ul>
 * Exécuté en tâche de fond : si MongoDB n'est pas joignable, l'application démarre quand même.
 *
 * <p>[Benjamin – #04, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@Component
public class AuditIndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(AuditIndexInitializer.class);

    private final MongoTemplate mongoTemplate;
    private final long retentionDays;
    private final boolean enabled;

    public AuditIndexInitializer(MongoTemplate mongoTemplate,
                                 @Value("${app.audit.retention-days:365}") long retentionDays,
                                 @Value("${app.audit.enabled:true}") boolean enabled) {
        this.mongoTemplate = mongoTemplate;
        this.retentionDays = retentionDays;
        this.enabled = enabled;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        if (!enabled) {
            return;
        }
        try {
            var ops = mongoTemplate.indexOps(AuditLog.class);
            ops.ensureIndex(new Index().on("timestamp", Sort.Direction.DESC)
                    .named("ttl_timestamp").expire(Duration.ofDays(retentionDays)));
            ops.ensureIndex(new Index().on("username", Sort.Direction.ASC).named("idx_username"));
            log.info("Index MongoDB du journal d'audit vérifiés (rétention {} jours)", retentionDays);
        } catch (RuntimeException e) {
            log.warn("Index MongoDB non créés (MongoDB indisponible ?) : {}", e.getMessage());
        }
    }
}
