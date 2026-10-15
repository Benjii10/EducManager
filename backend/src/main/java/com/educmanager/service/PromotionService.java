package com.educmanager.service;

import com.educmanager.dto.PromotionDto;
import com.educmanager.dto.PromotionRequest;
import com.educmanager.entity.Cursus;
import com.educmanager.entity.Promotion;
import com.educmanager.exception.BadRequestException;
import com.educmanager.exception.ConflictException;
import com.educmanager.exception.ResourceNotFoundException;
import com.educmanager.repository.CursusRepository;
import com.educmanager.repository.EnrollmentRepository;
import com.educmanager.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Règles métier des promotions :
 * nom unique, date de fin après la date de début, cursus existant,
 * suppression interdite s'il reste des inscrits.
 *
 * <p>[Benjamin – #03, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final CursusRepository cursusRepository;
    private final EnrollmentRepository enrollmentRepository;

    public PromotionService(PromotionRepository promotionRepository,
                            CursusRepository cursusRepository,
                            EnrollmentRepository enrollmentRepository) {
        this.promotionRepository = promotionRepository;
        this.cursusRepository = cursusRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<PromotionDto> findAll() {
        return promotionRepository.findAllWithCursus().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PromotionDto findById(Long id) {
        return toDto(get(id));
    }

    @Transactional
    public PromotionDto create(PromotionRequest request) {
        validate(request);
        if (promotionRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Une promotion porte déjà ce nom");
        }
        Promotion promotion = new Promotion();
        apply(promotion, request);
        return toDto(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionDto update(Long id, PromotionRequest request) {
        validate(request);
        Promotion promotion = get(id);
        if (promotionRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("Une promotion porte déjà ce nom");
        }
        apply(promotion, request);
        return toDto(promotionRepository.save(promotion));
    }

    @Transactional
    public void delete(Long id) {
        Promotion promotion = get(id);
        if (enrollmentRepository.existsByPromotionId(id)) {
            throw new ConflictException("Impossible de supprimer une promotion qui a des inscrits");
        }
        promotionRepository.delete(promotion);
    }

    Promotion get(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion introuvable : " + id));
    }

    private void validate(PromotionRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("La date de fin doit être postérieure à la date de début");
        }
    }

    private void apply(Promotion promotion, PromotionRequest request) {
        Cursus cursus = cursusRepository.findById(request.cursusId())
                .orElseThrow(() -> new ResourceNotFoundException("Cursus introuvable : " + request.cursusId()));
        promotion.setName(request.name().trim());
        promotion.setStartDate(request.startDate());
        promotion.setEndDate(request.endDate());
        promotion.setCursus(cursus);
    }

    private PromotionDto toDto(Promotion p) {
        return new PromotionDto(p.getId(), p.getName(), p.getStartDate(), p.getEndDate(),
                p.getCursus().getId(), p.getCursus().getName(),
                enrollmentRepository.countByPromotionId(p.getId()));
    }
}
