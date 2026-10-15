package com.educmanager.repository;

import com.educmanager.entity.CursusCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CursusCourseRepository extends JpaRepository<CursusCourse, Long> {

    @Query("""
            SELECT cc FROM CursusCourse cc
            JOIN FETCH cc.cursus
            JOIN FETCH cc.course
            WHERE cc.cursus.id = :cursusId
            ORDER BY cc.ordre ASC
            """)
    List<CursusCourse> findByCursusIdWithDetails(@Param("cursusId") Long cursusId);

    @Query("""
            SELECT cc FROM CursusCourse cc
            JOIN FETCH cc.cursus
            JOIN FETCH cc.course
            WHERE cc.id = :id
            """)
    Optional<CursusCourse> findByIdWithDetails(@Param("id") Long id);

    // [Benjamin – #03] un cours utilisé dans un cursus ne peut pas être supprimé
    boolean existsByCourse_Id(Long courseId);

    boolean existsByCursus_IdAndOrdre(Long cursusId, Integer ordre);

    boolean existsByCursus_IdAndOrdreAndIdNot(Long cursusId, Integer ordre, Long id);

    boolean existsByCursus_IdAndCourse_Id(Long cursusId, Long courseId);

    boolean existsByCursus_IdAndCourse_IdAndIdNot(Long cursusId, Long courseId, Long id);
}
