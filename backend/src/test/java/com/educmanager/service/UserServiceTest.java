package com.educmanager.service;

import com.educmanager.dto.CreateUserRequest;
import com.educmanager.dto.UpdateUserRequest;
import com.educmanager.dto.UserResponse;
import com.educmanager.entity.Student;
import com.educmanager.entity.UserAccount;
import com.educmanager.enums.Role;
import com.educmanager.exception.BadRequestException;
import com.educmanager.exception.ConflictException;
import com.educmanager.repository.EnrollmentRepository;
import com.educmanager.repository.StudentRepository;
import com.educmanager.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Gestion des comptes. [Benjamin – #06, 2026-10-15] Création. @author Benjamin */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4); // coût réduit : tests rapides
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userAccountRepository, studentRepository, enrollmentRepository, encoder);
        lenient().when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(inv -> {
            UserAccount u = inv.getArgument(0);
            if (u.getId() == null) {
                u.setId(42L);
            }
            return u;
        });
    }

    @Test
    void creeUnCompteEtudiantAvecSaFicheEleveEtUnMotDePasseHache() {
        when(userAccountRepository.existsByUsername("new@test.fr")).thenReturn(false);
        when(studentRepository.findByUserAccountId(42L)).thenReturn(Optional.empty());

        UserResponse res = service.create(new CreateUserRequest(" NEW@test.fr ", "Léa", "Durand", Role.STUDENT, "secret123"));

        ArgumentCaptor<UserAccount> user = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(user.capture());
        assertThat(user.getValue().getUsername()).isEqualTo("new@test.fr");
        assertThat(user.getValue().getPasswordHash()).isNotEqualTo("secret123").startsWith("$2");
        assertThat(encoder.matches("secret123", user.getValue().getPasswordHash())).isTrue();
        verify(studentRepository).save(any(Student.class));
        assertThat(res.role()).isEqualTo(Role.STUDENT);
    }

    @Test
    void refuseUnEmailDejaUtilise() {
        when(userAccountRepository.existsByUsername("dup@test.fr")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateUserRequest("dup@test.fr", "A", "B", Role.TEACHER, "secret123")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void unAdminNePeutPasSeSupprimerNiSeRetrograder() {
        UserAccount admin = new UserAccount();
        admin.setId(1L);
        admin.setUsername("admin@test.fr");
        admin.setRole(Role.ADMIN);
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.delete(1L, "admin@test.fr")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.update(1L, new UpdateUserRequest("A", "B", Role.TEACHER, null), "admin@test.fr"))
                .isInstanceOf(BadRequestException.class);
        verify(userAccountRepository, never()).delete(any());
    }

    @Test
    void refuseDeSupprimerUnEleveQuiADesInscriptions() {
        UserAccount user = new UserAccount();
        user.setId(5L);
        user.setUsername("eleve@test.fr");
        Student student = new Student();
        student.setId(9L);
        when(userAccountRepository.findById(5L)).thenReturn(Optional.of(user));
        when(studentRepository.findByUserAccountId(5L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudentId(9L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(5L, "admin@test.fr")).isInstanceOf(ConflictException.class);
    }

    @Test
    void unMotDePasseVideNeModifiePasLeMotDePasse() {
        UserAccount user = new UserAccount();
        user.setId(5L);
        user.setUsername("prof@test.fr");
        user.setRole(Role.TEACHER);
        user.setPasswordHash("ancien-hash");
        when(userAccountRepository.findById(5L)).thenReturn(Optional.of(user));
        when(studentRepository.findByUserAccountId(5L)).thenReturn(Optional.empty());

        service.update(5L, new UpdateUserRequest("Bob", "Martin", Role.TEACHER, ""), "admin@test.fr");

        assertThat(user.getPasswordHash()).isEqualTo("ancien-hash");
    }
}
