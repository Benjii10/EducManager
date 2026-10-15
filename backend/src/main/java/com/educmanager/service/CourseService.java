package com.educmanager.service;

import com.educmanager.entity.Course;
import com.educmanager.exception.ConflictException;
import com.educmanager.exception.ResourceNotFoundException;
import com.educmanager.repository.CourseRepository;
import com.educmanager.repository.CursusCourseRepository;
import com.educmanager.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Règles métier des cours.
 *
 * <p>[Benjamin – #03, 2026-10-15] Ajout de la lecture par id, de la modification et de la suppression,
 * de l'unicité du code et de l'interdiction de supprimer un cours utilisé
 * (dans un cursus ou par une inscription). Les méthodes existantes
 * {@code getAllCourses()} / {@code createCourse()} sont conservées (tests de Manar).</p>
 */
@Service
public class CourseService {

    private final CourseRepository repository;
    private final CursusCourseRepository cursusCourseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseService(CourseRepository repository,
                         CursusCourseRepository cursusCourseRepository,
                         EnrollmentRepository enrollmentRepository) {
        this.repository = repository;
        this.cursusCourseRepository = cursusCourseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public Course getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours introuvable : " + id));
    }

    public Course createCourse(Course course) {
        normalize(course);
        if (course.getCode() != null && repository.existsByCodeIgnoreCase(course.getCode())) {
            throw new ConflictException("Le code de cours " + course.getCode() + " existe déjà");
        }
        course.setId(null);
        return repository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, Course data) {
        Course existing = getById(id);
        normalize(data);
        if (repository.existsByCodeIgnoreCaseAndIdNot(data.getCode(), id)) {
            throw new ConflictException("Le code de cours " + data.getCode() + " existe déjà");
        }
        existing.setCode(data.getCode());
        existing.setTitre(data.getTitre());
        existing.setDescription(data.getDescription());
        existing.setDureeJours(data.getDureeJours());
        return repository.save(existing);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = getById(id);
        if (cursusCourseRepository.existsByCourse_Id(id)) {
            throw new ConflictException("Ce cours fait partie d'au moins un cursus : retirez-le d'abord des cursus");
        }
        if (enrollmentRepository.existsByCourseId(id)) {
            throw new ConflictException("Des élèves sont inscrits à ce cours");
        }
        repository.delete(course);
    }

    private void normalize(Course course) {
        if (course.getCode() != null) {
            course.setCode(course.getCode().trim().toUpperCase());
        }
        if (course.getTitre() != null) {
            course.setTitre(course.getTitre().trim());
        }
    }
}
