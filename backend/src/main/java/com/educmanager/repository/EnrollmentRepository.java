package com.educmanager.repository;

import com.educmanager.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** [Benjamin – #03, 2026-10-15] Méthodes adaptées aux nouvelles relations Promotion / Course. */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);

    boolean existsByStudentIdAndPromotionId(Long studentId, Long promotionId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentId(Long studentId);

    boolean existsByPromotionId(Long promotionId);

    boolean existsByCourseId(Long courseId);

    long countByPromotionId(Long promotionId);
}
