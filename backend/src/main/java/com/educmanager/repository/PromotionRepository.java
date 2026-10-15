package com.educmanager.repository;

import com.educmanager.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** [Benjamin – #03, 2026-10-15] Création. @author Benjamin */
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p JOIN FETCH p.cursus ORDER BY p.startDate DESC")
    List<Promotion> findAllWithCursus();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByCursus_Id(Long cursusId);
}
