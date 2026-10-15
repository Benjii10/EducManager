package com.educmanager.audit;

import com.educmanager.dto.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Écriture et consultation du journal d'audit.
 *
 * <p>L'écriture est asynchrone ({@code @Async}) et ne lève jamais d'exception : si MongoDB est
 * indisponible, l'application continue de fonctionner et l'erreur est seulement journalisée.</p>
 *
 * <p>[Benjamin – #04, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repository;
    private final MongoTemplate mongoTemplate;
    private final Clock clock;
    private final boolean enabled;

    public AuditService(AuditLogRepository repository,
                        MongoTemplate mongoTemplate,
                        Clock clock,
                        @Value("${app.audit.enabled:true}") boolean enabled) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
        this.clock = clock;
        this.enabled = enabled;
    }

    @Async
    public void record(AuditLog event) {
        if (!enabled) {
            return;
        }
        try {
            if (event.getTimestamp() == null) {
                event.setTimestamp(clock.instant());
            }
            repository.save(event);
        } catch (RuntimeException e) {
            log.warn("Audit non enregistré (MongoDB indisponible ?) : {} {} - {}",
                    event.getAction(), event.getPath(), e.getMessage());
        }
    }

    /** Recherche paginée, du plus récent au plus ancien, filtres optionnels (insensibles à la casse). */
    public PageResponse<AuditLog> search(String username, String action, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);

        Query query = new Query();
        if (username != null && !username.isBlank()) {
            query.addCriteria(Criteria.where("username").regex(Pattern.quote(username.trim()), "i"));
        }
        if (action != null && !action.isBlank()) {
            query.addCriteria(Criteria.where("action").is(action.trim().toUpperCase()));
        }
        long total = mongoTemplate.count(query, AuditLog.class);

        query.with(PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "timestamp")));
        List<AuditLog> content = mongoTemplate.find(query, AuditLog.class);
        return new PageResponse<>(content, total, safePage, safeSize);
    }
}
