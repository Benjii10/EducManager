package com.educmanager.controller;

import com.educmanager.dto.StudentDTO;
import com.educmanager.entity.Student;
import com.educmanager.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * API des élèves (lecture). La création d'un élève passe désormais par la gestion des comptes
 * (POST /api/users avec le rôle STUDENT) pour que chaque élève ait un compte de connexion.
 *
 * <p>[Benjamin – #03, 2026-10-15] La référente peut lister les élèves (nécessaire pour les
 * inscriptions) ; un élève ne peut lire que sa propre fiche (avant : n'importe laquelle,
 * faille OWASP A01) ; POST supprimé (doublon avec /api/users) ; injection par constructeur.</p>
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'REFERENTE', 'ADMIN')")
    public ResponseEntity<StudentDTO> getStudent(@PathVariable Long id, Authentication auth) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        boolean isStudent = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        boolean isOwn = student.getUserAccount() != null && student.getUserAccount().getUsername().equals(auth.getName());
        if (isStudent && !isOwn) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(toDto(student));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('REFERENTE', 'ADMIN')")
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .sorted(Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName))
                .map(StudentController::toDto)
                .toList();
    }

    private static StudentDTO toDto(Student s) {
        return new StudentDTO(s.getId(), s.getFirstName(), s.getLastName(), s.getEmail());
    }
}
