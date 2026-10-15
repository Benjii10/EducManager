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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Règles métier des promotions. [Benjamin – #06, 2026-10-15] Création. @author Benjamin */
@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock private PromotionRepository promotionRepository;
    @Mock private CursusRepository cursusRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @InjectMocks private PromotionService service;

    private final Cursus cursus = Cursus.builder().id(1L).name("CDA").build();

    private PromotionRequest request(LocalDate start, LocalDate end) {
        return new PromotionRequest("  CDA 2027 ", start, end, 1L);
    }

    @Test
    void creeUnePromotionValide() {
        when(promotionRepository.existsByNameIgnoreCase("CDA 2027")).thenReturn(false);
        when(cursusRepository.findById(1L)).thenReturn(Optional.of(cursus));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> {
            Promotion p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PromotionDto dto = service.create(request(LocalDate.of(2027, 1, 4), LocalDate.of(2027, 6, 30)));

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.name()).isEqualTo("CDA 2027");   // espaces retirés
        assertThat(dto.cursusName()).isEqualTo("CDA");
    }

    @Test
    void refuseUneDateDeFinAvantLaDateDeDebut() {
        assertThatThrownBy(() -> service.create(request(LocalDate.of(2027, 6, 30), LocalDate.of(2027, 1, 4))))
                .isInstanceOf(BadRequestException.class);
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void refuseUnNomDejaUtilise() {
        when(promotionRepository.existsByNameIgnoreCase("CDA 2027")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(LocalDate.of(2027, 1, 4), LocalDate.of(2027, 6, 30))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void refuseUnCursusInexistant() {
        when(cursusRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request(LocalDate.of(2027, 1, 4), LocalDate.of(2027, 6, 30))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void refuseDeSupprimerUnePromotionAvecInscrits() {
        when(promotionRepository.findById(5L)).thenReturn(Optional.of(Promotion.builder().id(5L).cursus(cursus).build()));
        when(enrollmentRepository.existsByPromotionId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(ConflictException.class);
        verify(promotionRepository, never()).delete(any());
    }
}
