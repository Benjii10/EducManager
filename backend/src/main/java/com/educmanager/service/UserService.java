package com.educmanager.service;

import com.educmanager.dto.CreateUserRequest;
import com.educmanager.dto.UpdateUserRequest;
import com.educmanager.dto.UserResponse;
import com.educmanager.entity.Student;
import com.educmanager.entity.UserAccount;
import com.educmanager.enums.Role;
import com.educmanager.exception.BadRequestException;
import com.educmanager.exception.ConflictException;
import com.educmanager.exception.ResourceNotFoundException;
import com.educmanager.repository.EnrollmentRepository;
import com.educmanager.repository.StudentRepository;
import com.educmanager.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Gestion des comptes par l'administrateur.
 * <ul>
 *   <li>email unique, stocké en minuscules ; mot de passe haché en BCrypt ;</li>
 *   <li>un compte STUDENT a toujours une fiche {@link Student} liée (créée automatiquement) ;</li>
 *   <li>un administrateur ne peut pas supprimer ni rétrograder son propre compte ;</li>
 *   <li>suppression refusée si l'élève a des inscriptions.</li>
 * </ul>
 *
 * <p>[Benjamin – #03, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository userAccountRepository,
                       StudentRepository studentRepository,
                       EnrollmentRepository enrollmentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userAccountRepository.findAll().stream()
                .sorted(Comparator.comparing(UserAccount::getUsername))
                .map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userAccountRepository.existsByUsername(email)) {
            throw new ConflictException("Un compte existe déjà avec cet email");
        }
        UserAccount user = new UserAccount();
        user.setUsername(email);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(request.role());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = userAccountRepository.save(user);

        if (request.role() == Role.STUDENT) {
            syncStudent(user);
        }
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request, String currentUsername) {
        UserAccount user = get(id);
        if (user.getUsername().equals(currentUsername) && request.role() != Role.ADMIN) {
            throw new BadRequestException("Vous ne pouvez pas retirer votre propre rôle administrateur");
        }
        Optional<Student> student = studentRepository.findByUserAccountId(id);
        if (user.getRole() == Role.STUDENT && request.role() != Role.STUDENT && student.isPresent()
                && enrollmentRepository.existsByStudentId(student.get().getId())) {
            throw new ConflictException("Cet élève a des inscriptions : son rôle ne peut pas être changé");
        }

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(request.role());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user = userAccountRepository.save(user);

        if (request.role() == Role.STUDENT) {
            syncStudent(user);
        } else {
            student.ifPresent(studentRepository::delete);
        }
        return toResponse(user);
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        UserAccount user = get(id);
        if (user.getUsername().equals(currentUsername)) {
            throw new BadRequestException("Vous ne pouvez pas supprimer votre propre compte");
        }
        Optional<Student> student = studentRepository.findByUserAccountId(id);
        if (student.isPresent()) {
            if (enrollmentRepository.existsByStudentId(student.get().getId())) {
                throw new ConflictException("Cet élève a des inscriptions : supprimez-les d'abord");
            }
            studentRepository.delete(student.get());
        }
        userAccountRepository.delete(user);
    }

    private UserAccount get(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    /** Crée ou met à jour la fiche élève liée à un compte STUDENT. */
    private void syncStudent(UserAccount user) {
        Student student = studentRepository.findByUserAccountId(user.getId()).orElseGet(Student::new);
        student.setUserAccount(user);
        student.setEmail(user.getUsername());
        student.setFirstName(user.getFirstName());
        student.setLastName(user.getLastName());
        studentRepository.save(student);
    }

    private UserResponse toResponse(UserAccount user) {
        Long studentId = studentRepository.findByUserAccountId(user.getId()).map(Student::getId).orElse(null);
        return new UserResponse(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(),
                user.getRole(), studentId);
    }
}
