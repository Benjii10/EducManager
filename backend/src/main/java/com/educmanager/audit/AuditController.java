package com.educmanager.audit;

import com.educmanager.dto.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultation du journal d'audit, réservée à l'administrateur.
 * {@code GET /api/audit?username=&action=&page=0&size=20}
 *
 * <p>[Benjamin – #04, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public PageResponse<AuditLog> search(@RequestParam(required = false) String username,
                                         @RequestParam(required = false) String action,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return auditService.search(username, action, page, size);
    }
}
