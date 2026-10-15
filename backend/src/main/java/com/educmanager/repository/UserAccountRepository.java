package com.educmanager.repository;

import com.educmanager.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);

    // [Benjamin – #03] unicité de l'email lors de la création d'un compte
    boolean existsByUsername(String username);
}