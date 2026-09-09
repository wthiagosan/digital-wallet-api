package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Ada Lovelace", "12345678901", "ada@example.com");
    }

    @Test
    @DisplayName("Should save and find user by document number")
    void shouldSaveAndFindByDocumentNumber() {
        entityManager.persistAndFlush(sampleUser);

        Optional<User> found = userRepository.findByDocumentNumber("12345678901");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Ada Lovelace");
        assertThat(found.get().getEmail()).isEqualTo("ada@example.com");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should save and find user by email")
    void shouldSaveAndFindByEmail() {
        entityManager.persistAndFlush(sampleUser);

        Optional<User> found = userRepository.findByEmail("ada@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getDocumentNumber()).isEqualTo("12345678901");
    }

    @Test
    @DisplayName("Should return true when document or email exists")
    void shouldCheckExistenceByDocumentAndEmail() {
        entityManager.persistAndFlush(sampleUser);

        assertThat(userRepository.existsByDocumentNumber("12345678901")).isTrue();
        assertThat(userRepository.existsByDocumentNumber("99999999999")).isFalse();
        assertThat(userRepository.existsByEmail("ada@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when saving duplicate document number")
    void shouldFailOnDuplicateDocument() {
        entityManager.persistAndFlush(sampleUser);

        User duplicate = new User("Alan Turing", "12345678901", "alan@example.com");

        assertThatThrownBy(() -> {
            userRepository.saveAndFlush(duplicate);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when saving duplicate email")
    void shouldFailOnDuplicateEmail() {
        entityManager.persistAndFlush(sampleUser);

        User duplicate = new User("Alan Turing", "98765432100", "ada@example.com");

        assertThatThrownBy(() -> {
            userRepository.saveAndFlush(duplicate);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
