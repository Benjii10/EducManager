package com.educmanager.controller;

import com.educmanager.dto.PromotionDto;
import com.educmanager.dto.PromotionRequest;
import com.educmanager.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API des promotions. Lecture : connecté ; écriture : REFERENTE / ADMIN (SecurityConfig).
 *
 * <p>[Benjamin – #03, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService service;

    public PromotionController(PromotionService service) {
        this.service = service;
    }

    @GetMapping
    public List<PromotionDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PromotionDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<PromotionDto> create(@Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public PromotionDto update(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
