package com.educmanager.service;

import com.educmanager.dto.EnrollmentRequest;
import com.educmanager.dto.EnrollmentResponse;
import com.educmanager.entity.Course;
import com.educmanager.entity.Cursus;
import com.educmanager.entity.Enrollment;
import com.educmanager.entity.Promotion;
import com.educmanager.entity.Student;
import com.educmanager.enums.EnrollmentType;
import com.educmanager.exception.BadRequestException;
import com.educmanager.exception.ConflictException;
import com.educmanager.repository.CourseRepository;
import com.educmanager.repository.CursusCourseRepository;
import com.educmanager.repository.EnrollmentRepository;
import com.educmanager.repository.PromotionRepository;
import com.educmanager.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Règles métier des inscriptions. [Benjamin – #06, 2026-10-15] Création. @author Benjamin */
@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private PromotionRepository promotionRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CursusCourseRepository cursusCourseRepository;

    private EnrollmentService service;
    private Student student;
    private final Cursus cursus = Cursus.builder().id(1L).name("CDA").build();

    @BeforeEach
    void setUp() {
        // date du jour figée : 15/10/2026
        Clock clock = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), ZoneId.of("UTC"));
        service = new EnrollmentService(enrollmentRepository, studentRepository, promotionRepository,
                courseRepository, cursusCourseRepository, clock);
        student = new Student();
        student.setId(7L);
        student.setFirstName("Claire");
        student.setLastName("Bernard");
    }

    private Promotion promotion(LocalDate end) {
        return Promotion.builder().id(3L).name("CDA 2026").cursus(cursus)
                .startDate(LocalDate.of(2026, 9, 7)).endDate(end).build();
    }

    @Test
    void inscritUnElevePromotion() {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(promotionRepository.findById(3L)).thenReturn(Optional.of(promotion(LocalDate.of(2027, 6, 30))));
        when(enrollmentRepository.existsByStudentIdAndPromotionId(7L, 3L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentResponse res = service.enroll(new EnrollmentRequest(7L, EnrollmentType.PROMOTION, 3L, null, false));

        assertThat(res.promotionName()).isEqualTo("CDA 2026");
        assertThat(res.studentName()).isEqualTo("Claire Bernard");
        assertThat(res.enrollmentDate()).isEqualTo(LocalDate.of(2026, 10, 15));
    }

    @Test
    void refuseUneDoubleInscription() {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(promotionRepository.findById(3L)).thenReturn(Optional.of(promotion(LocalDate.of(2027, 6, 30))));
        when(enrollmentRepository.existsByStudentIdAndPromotionId(7L, 3L)).thenReturn(true);

        assertThatThrownBy(() -> service.enroll(new EnrollmentRequest(7L, EnrollmentType.PROMOTION, 3L, null, false)))
                .isInstanceOf(ConflictException.class);
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void refusePromotionTermineeSaufSiForcee() {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(promotionRepository.findById(3L)).thenReturn(Optional.of(promotion(LocalDate.of(2026, 1, 31))));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> service.enroll(new EnrollmentRequest(7L, EnrollmentType.PROMOTION, 3L, null, false)))
                .isInstanceOf(ConflictException.class);

        EnrollmentResponse forced = service.enroll(new EnrollmentRequest(7L, EnrollmentType.PROMOTION, 3L, null, true));
        assertThat(forced.forced()).isTrue();
    }

    @Test
    void uneInscriptionUnitaireExigeUnCours() {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> service.enroll(new EnrollmentRequest(7L, EnrollmentType.UNIT, null, null, false)))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.enroll(new EnrollmentRequest(7L, EnrollmentType.UNIT, 3L, 2L, false)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void inscritUnEleveAUnCoursALUnite() {
        Course course = Course.builder().id(2L).code("NG-01").titre("Angular").build();
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(7L, 2L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentResponse res = service.enroll(new EnrollmentRequest(7L, EnrollmentType.UNIT, null, 2L, false));

        assertThat(res.courseTitle()).isEqualTo("Angular");
        assertThat(res.promotionId()).isNull();
    }
}
