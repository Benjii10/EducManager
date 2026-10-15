package com.educmanager.controller;

import com.educmanager.dto.EnrollmentRequest;
import com.educmanager.dto.EnrollmentResponse;
import com.educmanager.dto.PlanningItem;
import com.educmanager.exception.ResourceNotFoundException;
import com.educmanager.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API des inscriptions.
 *
 * <p>[Benjamin – #03, 2026-10-15] Réécriture : la logique métier est déplacée dans
 * {@link EnrollmentService} (contrôleur « fin »), validation des données, nouvelles routes
 * (liste, suppression, inscriptions et planning de l'élève connecté) et contrôle
 * qu'un élève ne consulte que SES inscriptions (avant : n'importe quel élève pouvait lire
 * celles des autres en changeant l'id dans l'URL — faille OWASP A01).</p>
 */
@RestController
@RequestMapping("/api/enrollment")
public class EnrollmentController {

    private final EnrollmentService service;

    public EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('REFERENTE', 'ADMIN')")
    public List<EnrollmentResponse> findAll() {
        return service.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REFERENTE', 'ADMIN')")
    public ResponseEntity<EnrollmentResponse> enrollStudent(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enroll(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('REFERENTE', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'REFERENTE', 'ADMIN')")
    public List<EnrollmentResponse> getStudentEnrollments(@PathVariable Long studentId, Authentication auth) {
        if (isStudent(auth) && !isOwnStudent(auth, studentId)) {
            throw new AccessDeniedException("Un élève ne peut consulter que ses propres inscriptions");
        }
        return service.findByStudent(studentId);
    }

    /** Inscriptions de l'élève connecté. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<EnrollmentResponse> myEnrollments(Authentication auth) {
        return service.findByUsername(auth.getName());
    }

    /** Planning prévisionnel de l'élève connecté (page Calendrier). */
    @GetMapping("/me/planning")
    @PreAuthorize("hasRole('STUDENT')")
    public List<PlanningItem> myPlanning(Authentication auth) {
        return service.planningOf(auth.getName());
    }

    private static boolean isStudent(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
    }

    private boolean isOwnStudent(Authentication auth, Long studentId) {
        try {
            return service.studentOf(auth.getName()).getId().equals(studentId);
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
}
