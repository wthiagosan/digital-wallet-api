package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.request.CreateUserRequest;
import com.portfolio.wallet.dto.response.UserResponse;
import com.portfolio.wallet.exception.DocumentAlreadyExistsException;
import com.portfolio.wallet.exception.EmailAlreadyExistsException;
import com.portfolio.wallet.exception.UserNotFoundException;
import com.portfolio.wallet.model.User;
import com.portfolio.wallet.model.Wallet;
import com.portfolio.wallet.repository.UserRepository;
import com.portfolio.wallet.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CreateUserRequest("Grace Hopper", "12345678901", "grace@navy.mil");
    }

    @Test
    @DisplayName("Should create user and linked wallet with zero balance")
    void shouldCreateUserSuccessfully() {
        when(userRepository.existsByDocumentNumber(validRequest.documentNumber())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.email())).thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            if (user.getWallet() != null) {
                user.getWallet().setId(10L);
            }
            return user;
        });

        UserResponse response = userService.createUser(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.fullName()).isEqualTo("Grace Hopper");
        assertThat(response.documentNumber()).isEqualTo("12345678901");
        assertThat(response.email()).isEqualTo("grace@navy.mil");
        assertThat(response.walletId()).isEqualTo(10L);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DocumentAlreadyExistsException when document is already registered")
    void shouldFailWhenDocumentExists() {
        when(userRepository.existsByDocumentNumber(validRequest.documentNumber())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(DocumentAlreadyExistsException.class)
                .hasMessageContaining("12345678901");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email is already registered")
    void shouldFailWhenEmailExists() {
        when(userRepository.existsByDocumentNumber(validRequest.documentNumber())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("grace@navy.mil");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void shouldFindById() {
        User user = new User(1L, "Grace Hopper", "12345678901", "grace@navy.mil", LocalDateTime.now());
        Wallet wallet = new Wallet(10L, user, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now());
        user.setWallet(wallet);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.fullName()).isEqualTo("Grace Hopper");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user is not found by ID")
    void shouldFailFindByIdWhenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should list all registered users")
    void shouldFindAllUsers() {
        User u1 = new User(1L, "User 1", "111", "u1@mail.com", LocalDateTime.now());
        User u2 = new User(2L, "User 2", "222", "u2@mail.com", LocalDateTime.now());

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponse> list = userService.findAll();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).fullName()).isEqualTo("User 1");
        assertThat(list.get(1).fullName()).isEqualTo("User 2");
    }
}
