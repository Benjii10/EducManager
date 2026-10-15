package com.educmanager.service;

import com.educmanager.dto.EnrollmentRequest;
import com.educmanager.dto.EnrollmentResponse;
import com.educmanager.dto.PlanningItem;
import com.educmanager.entity.Course;
import com.educmanager.entity.Enrollment;
import com.educmanager.entity.Promotion;
import com.educmanager.entity.Student;
import com.educmanager.enums.EnrollmentType;
import com.educmanager.exception.BadRequestException;
import com.educmanager.exception.ConflictException;
import com.educmanager.exception.ResourceNotFoundException;
import com.educmanager.repository.CourseRepository;
import com.educmanager.repository.CursusCourseRepository;
import com.educmanager.repository.EnrollmentRepository;
import com.educmanager.repository.PromotionRepository;
import com.educmanager.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Règles métier des inscriptions (déplacées depuis EnrollmentController) :
 * <ul>
 *   <li>PROMOTION → promotion obligatoire, UNIT → cours obligatoire (et pas les deux) ;</li>
 *   <li>pas de double inscription (409) ;</li>
 *   <li>inscription dans une promotion terminée refusée sauf si {@code forced = true}.</li>
 * </ul>
 * Calcule aussi le planning d'un élève ({@link PlanningCalculator}).
 *
 * <p>[Benjamin – #03, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final PromotionRepository promotionRepository;
    private final CourseRepository courseRepository;
    private final CursusCourseRepository cursusCourseRepository;
    private final Clock clock;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository,
                             PromotionRepository promotionRepository,
                             CourseRepository courseRepository,
                             CursusCourseRepository cursusCourseRepository,
                             Clock clock) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.promotionRepository = promotionRepository;
        this.courseRepository = courseRepository;
        this.cursusCourseRepository = cursusCourseRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findAll() {
        return enrollmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Enrollment::getEnrollmentDate).reversed())
                .map(EnrollmentService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Élève introuvable : " + studentId);
        }
        return enrollmentRepository.findByStudentId(studentId).stream().map(EnrollmentService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByUsername(String username) {
        return findByStudent(studentOf(username).getId());
    }

    @Transactional
    public EnrollmentResponse enroll(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Élève introuvable : " + request.studentId()));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setType(request.type());
        enrollment.setForced(request.isForced());
        enrollment.setEnrollmentDate(LocalDate.now(clock));

        if (request.type() == EnrollmentType.PROMOTION) {
            if (request.promotionId() == null || request.courseId() != null) {
                throw new BadRequestException("Une inscription PROMOTION nécessite une promotion (et pas de cours)");
            }
            Promotion promotion = promotionRepository.findById(request.promotionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Promotion introuvable : " + request.promotionId()));
            if (enrollmentRepository.existsByStudentIdAndPromotionId(student.getId(), promotion.getId())) {
                throw new ConflictException("Cet élève est déjà inscrit à cette promotion");
            }
            if (promotion.getEndDate().isBefore(LocalDate.now(clock)) && !request.isForced()) {
                throw new ConflictException("Cette promotion est terminée : cochez « forcer l'inscription » pour confirmer");
            }
            enrollment.setPromotion(promotion);
        } else {
            if (request.courseId() == null || request.promotionId() != null) {
                throw new BadRequestException("Une inscription UNIT nécessite un cours (et pas de promotion)");
            }
            Course course = courseRepository.findById(request.courseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cours introuvable : " + request.courseId()));
            if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
                throw new ConflictException("Cet élève est déjà inscrit à ce cours");
            }
            enrollment.setCourse(course);
        }

        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public void delete(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inscription introuvable : " + id);
        }
        enrollmentRepository.deleteById(id);
    }

    /** Planning de l'élève connecté : cours de ses promotions datés + cours à l'unité (non datés). */
    @Transactional(readOnly = true)
    public List<PlanningItem> planningOf(String username) {
        Student student = studentOf(username);
        List<PlanningItem> items = new ArrayList<>();
        for (Enrollment e : enrollmentRepository.findByStudentId(student.getId())) {
            if (e.getType() == EnrollmentType.PROMOTION && e.getPromotion() != null) {
                Promotion p = e.getPromotion();
                items.addAll(PlanningCalculator.compute(p,
                        cursusCourseRepository.findByCursusIdWithDetails(p.getCursus().getId())));
            } else if (e.getCourse() != null) {
                items.add(new PlanningItem(null, null, e.getCourse().getCode(), e.getCourse().getTitre(), null, null));
            }
        }
        items.sort(Comparator.comparing(PlanningItem::startDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return items;
    }

    public Student studentOf(String username) {
        return studentRepository.findByUserAccountUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune fiche élève pour ce compte"));
    }

    static EnrollmentResponse toResponse(Enrollment e) {
        Student s = e.getStudent();
        return new EnrollmentResponse(
                e.getId(),
                s.getId(),
                s.getFirstName() + " " + s.getLastName(),
                e.getType(),
                e.getPromotion() != null ? e.getPromotion().getId() : null,
                e.getPromotion() != null ? e.getPromotion().getName() : null,
                e.getCourse() != null ? e.getCourse().getId() : null,
                e.getCourse() != null ? e.getCourse().getTitre() : null,
                e.getEnrollmentDate(),
                e.isForced());
    }
}
