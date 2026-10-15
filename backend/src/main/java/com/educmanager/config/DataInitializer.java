package com.educmanager.config;

import com.educmanager.entity.Course;
import com.educmanager.entity.Cursus;
import com.educmanager.entity.CursusCourse;
import com.educmanager.entity.Enrollment;
import com.educmanager.entity.Filiere;
import com.educmanager.entity.Promotion;
import com.educmanager.entity.Student;
import com.educmanager.entity.UserAccount;
import com.educmanager.enums.EnrollmentType;
import com.educmanager.enums.Role;
import com.educmanager.repository.CourseRepository;
import com.educmanager.repository.CursusCourseRepository;
import com.educmanager.repository.CursusRepository;
import com.educmanager.repository.EnrollmentRepository;
import com.educmanager.repository.FiliereRepository;
import com.educmanager.repository.PromotionRepository;
import com.educmanager.repository.StudentRepository;
import com.educmanager.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Jeu de données de démonstration, chargé au démarrage si la base est vide.
 *
 * <p>[Benjamin – #01, 2026-10-15] Création. Désactivable avec {@code app.seed.enabled=false}
 * (à faire en production).</p>
 *
 * <p>[Benjamin – #03, 2026-10-15] Ajout : cours ordonnés dans le cursus CDA, deux promotions
 * (une en cours, une terminée) et l'inscription de l'étudiante de démonstration à la promotion
 * en cours (pour que son calendrier ne soit pas vide).</p>
 *
 * <p>Comptes créés (identifiant = email) :</p>
 * <pre>
 *  admin@educmanager.fr      / admin123      → ADMIN
 *  referente@educmanager.fr  / referente123  → REFERENTE
 *  formateur@educmanager.fr  / formateur123  → TEACHER
 *  etudiant@educmanager.fr   / etudiant123   → STUDENT (+ fiche Student liée)
 * </pre>
 *
 * @author Benjamin
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserAccountRepository userAccountRepository;
    private final StudentRepository studentRepository;
    private final FiliereRepository filiereRepository;
    private final CursusRepository cursusRepository;
    private final CourseRepository courseRepository;
    private final CursusCourseRepository cursusCourseRepository;
    private final PromotionRepository promotionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserAccountRepository userAccountRepository,
                           StudentRepository studentRepository,
                           FiliereRepository filiereRepository,
                           CursusRepository cursusRepository,
                           CourseRepository courseRepository,
                           CursusCourseRepository cursusCourseRepository,
                           PromotionRepository promotionRepository,
                           EnrollmentRepository enrollmentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.studentRepository = studentRepository;
        this.filiereRepository = filiereRepository;
        this.cursusRepository = cursusRepository;
        this.courseRepository = courseRepository;
        this.cursusCourseRepository = cursusCourseRepository;
        this.promotionRepository = promotionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userAccountRepository.count() == 0) {
            createUser("admin@educmanager.fr", "admin123", Role.ADMIN, "Alice", "Dupont");
            createUser("referente@educmanager.fr", "referente123", Role.REFERENTE, "Sophie", "Leroy");
            createUser("formateur@educmanager.fr", "formateur123", Role.TEACHER, "Bob", "Martin");
            UserAccount etudiant = createUser("etudiant@educmanager.fr", "etudiant123", Role.STUDENT, "Claire", "Bernard");

            Student student = new Student();
            student.setFirstName("Claire");
            student.setLastName("Bernard");
            student.setEmail("etudiant@educmanager.fr");
            student.setUserAccount(etudiant);
            studentRepository.save(student);
            log.info("[DataInitializer] 4 comptes de démonstration créés");
        }

        if (filiereRepository.count() == 0) {
            Filiere dev = filiereRepository.save(Filiere.builder().name("Développement").build());
            Filiere reseau = filiereRepository.save(Filiere.builder().name("Réseaux et systèmes").build());

            Cursus cda = cursusRepository.save(Cursus.builder().name("Concepteur Développeur d'Applications").filiere(dev).build());
            Cursus dwwm = cursusRepository.save(Cursus.builder().name("Développeur Web et Web Mobile").filiere(dev).build());
            cursusRepository.save(Cursus.builder().name("Administrateur Systèmes et Réseaux").filiere(reseau).build());

            Course java = courseRepository.save(Course.builder().code("JAVA-01").titre("Java et POO")
                    .description("Bases du langage Java et programmation orientée objet").dureeJours(10).build());
            Course spring = courseRepository.save(Course.builder().code("SPRING-01").titre("Spring Boot")
                    .description("API REST, JPA et Spring Security").dureeJours(10).build());
            Course angular = courseRepository.save(Course.builder().code("NG-01").titre("Angular")
                    .description("Composants, services, routing et appels HTTP").dureeJours(8).build());
            Course devops = courseRepository.save(Course.builder().code("DEVOPS-01").titre("Docker et CI/CD")
                    .description("Conteneurs, docker compose, intégration et déploiement continus").dureeJours(5).build());

            cursusCourseRepository.save(CursusCourse.builder().cursus(cda).course(java).ordre(1).build());
            cursusCourseRepository.save(CursusCourse.builder().cursus(cda).course(spring).ordre(2).build());
            cursusCourseRepository.save(CursusCourse.builder().cursus(cda).course(angular).ordre(3).build());
            cursusCourseRepository.save(CursusCourse.builder().cursus(cda).course(devops).ordre(4).build());
            cursusCourseRepository.save(CursusCourse.builder().cursus(dwwm).course(angular).ordre(1).build());

            Promotion cda2026 = promotionRepository.save(Promotion.builder().name("CDA 2026-2027").cursus(cda)
                    .startDate(LocalDate.of(2026, 9, 7)).endDate(LocalDate.of(2027, 6, 30)).build());
            promotionRepository.save(Promotion.builder().name("DWWM 2025").cursus(dwwm)
                    .startDate(LocalDate.of(2025, 1, 6)).endDate(LocalDate.of(2025, 7, 4)).build());

            studentRepository.findByUserAccountUsername("etudiant@educmanager.fr").ifPresent(student -> {
                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(student);
                enrollment.setType(EnrollmentType.PROMOTION);
                enrollment.setPromotion(cda2026);
                enrollment.setEnrollmentDate(LocalDate.of(2026, 9, 1));
                enrollmentRepository.save(enrollment);
            });

            log.info("[DataInitializer] filières, cursus, cours, promotions et inscription de démonstration créés");
        }
    }

    private UserAccount createUser(String email, String rawPassword, Role role, String firstName, String lastName) {
        UserAccount user = new UserAccount();
        user.setUsername(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return userAccountRepository.save(user);
    }
}
