package com.educmanager.repository;

import com.educmanager.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // [Benjamin – #03, 2026-10-15] fiche élève d'un compte utilisateur (connexion étudiant, gestion des comptes)
    Optional<Student> findByUserAccountId(Long userAccountId);

    Optional<Student> findByUserAccountUsername(String username);

    boolean existsByEmailIgnoreCase(String email);
}
