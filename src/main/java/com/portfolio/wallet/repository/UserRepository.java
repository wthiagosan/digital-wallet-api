package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByDocumentNumber(String documentNumber);

    Optional<User> findByEmail(String email);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByEmail(String email);
}
