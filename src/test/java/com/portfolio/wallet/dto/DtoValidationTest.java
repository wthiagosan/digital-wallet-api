package com.portfolio.wallet.dto;

import com.portfolio.wallet.dto.request.CreateUserRequest;
import com.portfolio.wallet.dto.request.DepositRequest;
import com.portfolio.wallet.dto.request.TransferRequest;
import com.portfolio.wallet.dto.response.TransactionResponse;
import com.portfolio.wallet.dto.response.UserResponse;
import com.portfolio.wallet.dto.response.WalletResponse;
import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.User;
import com.portfolio.wallet.model.Wallet;
import com.portfolio.wallet.model.enums.TransactionStatus;
import com.portfolio.wallet.model.enums.TransactionType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Should validate valid CreateUserRequest successfully")
    void shouldPassValidCreateUserRequest() {
        var request = new CreateUserRequest("Grace Hopper", "12345678901", "grace@navy.mil");
        var violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject CreateUserRequest with invalid email and blank name")
    void shouldFailInvalidCreateUserRequest() {
        var request = new CreateUserRequest("", "123", "invalid-email");
        var violations = validator.validate(request);

        assertThat(violations).hasSize(4);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fullName"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("documentNumber"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Should validate DepositRequest with positive amount")
    void shouldPassValidDepositRequest() {
        var request = new DepositRequest(new BigDecimal("100.00"));
        var violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject DepositRequest with zero or negative amount")
    void shouldFailNonPositiveDepositRequest() {
        var zeroRequest = new DepositRequest(BigDecimal.ZERO);
        var negativeRequest = new DepositRequest(new BigDecimal("-10.00"));
        var nullRequest = new DepositRequest(null);

        assertThat(validator.validate(zeroRequest)).isNotEmpty();
        assertThat(validator.validate(negativeRequest)).isNotEmpty();
        assertThat(validator.validate(nullRequest)).isNotEmpty();
    }

    @Test
    @DisplayName("Should validate TransferRequest with positive amount and valid wallet IDs")
    void shouldPassValidTransferRequest() {
        var request = new TransferRequest(1L, 2L, new BigDecimal("50.00"));
        var violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject TransferRequest with null IDs or zero amount")
    void shouldFailInvalidTransferRequest() {
        var request = new TransferRequest(null, null, BigDecimal.ZERO);
        var violations = validator.validate(request);

        assertThat(violations).hasSize(3);
    }

    @Test
    @DisplayName("Should reject DepositRequest with more than 2 decimal places (anti-fractional exploit)")
    void shouldFailDepositRequestWithExcessiveFractionalDigits() {
        var request = new DepositRequest(new BigDecimal("10.12345"));
        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("2 casas decimais"));
    }

    @Test
    @DisplayName("Should reject TransferRequest with negative wallet IDs")
    void shouldFailTransferRequestWithNegativeWalletIds() {
        var request = new TransferRequest(-1L, 2L, new BigDecimal("50.00"));
        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("maior que zero"));
    }

    @Test
    @DisplayName("Should reject TransferRequest exceeding maximum transaction threshold")
    void shouldFailTransferRequestWithExceededMaxAmount() {
        var request = new TransferRequest(1L, 2L, new BigDecimal("2000000000.00"));
        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("valor máximo permitido"));
    }

    @Test
    @DisplayName("Should correctly map User and Wallet to UserResponse")
    void shouldMapUserResponse() {
        User user = new User(1L, "Linus Torvalds", "11122233344", "linus@kernel.org", LocalDateTime.now());
        Wallet wallet = new Wallet(10L, user, new BigDecimal("1000.00"), LocalDateTime.now(), LocalDateTime.now());
        user.setWallet(wallet);

        UserResponse response = UserResponse.fromEntity(user);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.fullName()).isEqualTo("Linus Torvalds");
        assertThat(response.documentNumber()).isEqualTo("11122233344");
        assertThat(response.email()).isEqualTo("linus@kernel.org");
        assertThat(response.walletId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should correctly map Wallet to WalletResponse")
    void shouldMapWalletResponse() {
        User user = new User(2L, "Barbara Liskov", "55566677788", "barbara@mit.edu", LocalDateTime.now());
        Wallet wallet = new Wallet(20L, user, new BigDecimal("350.50"), LocalDateTime.now(), LocalDateTime.now());

        WalletResponse response = WalletResponse.fromEntity(wallet);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("350.50"));
    }

    @Test
    @DisplayName("Should correctly map Transaction to TransactionResponse")
    void shouldMapTransactionResponse() {
        Wallet source = new Wallet();
        source.setId(1L);

        Wallet target = new Wallet();
        target.setId(2L);

        Transaction tx = new Transaction(100L, source, target, new BigDecimal("80.00"),
                TransactionType.TRANSFER, TransactionStatus.COMPLETED, LocalDateTime.now());

        TransactionResponse response = TransactionResponse.fromEntity(tx);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.sourceWalletId()).isEqualTo(1L);
        assertThat(response.targetWalletId()).isEqualTo(2L);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(response.transactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);
    }
}
