package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.User;
import com.portfolio.wallet.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User sampleUser;
    private Wallet sampleWallet;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Margaret Hamilton", "22233344455", "margaret@nasa.gov");
        entityManager.persistAndFlush(sampleUser);

        sampleWallet = new Wallet(sampleUser, new BigDecimal("150.00"));
        entityManager.persistAndFlush(sampleWallet);
    }

    @Test
    @DisplayName("Should find wallet by associated user id")
    void shouldFindWalletByUserId() {
        Optional<Wallet> found = walletRepository.findByUserId(sampleUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(found.get().getUser().getFullName()).isEqualTo("Margaret Hamilton");
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find wallet with pessimistic write lock")
    void shouldFindWalletWithLock() {
        Optional<Wallet> lockedWallet = walletRepository.findByIdWithLock(sampleWallet.getId());

        assertThat(lockedWallet).isPresent();
        assertThat(lockedWallet.get().getId()).isEqualTo(sampleWallet.getId());
        assertThat(lockedWallet.get().getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should update balance correctly through rich domain deposit method")
    void shouldDepositFunds() {
        Wallet wallet = walletRepository.findById(sampleWallet.getId()).orElseThrow();
        wallet.deposit(new BigDecimal("50.25"));

        walletRepository.saveAndFlush(wallet);
        entityManager.clear();

        Wallet updated = walletRepository.findById(sampleWallet.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo(new BigDecimal("200.25"));
    }

    @Test
    @DisplayName("Should update balance correctly through rich domain withdraw method")
    void shouldWithdrawFunds() {
        Wallet wallet = walletRepository.findById(sampleWallet.getId()).orElseThrow();
        wallet.withdraw(new BigDecimal("50.00"));

        walletRepository.saveAndFlush(wallet);
        entityManager.clear();

        Wallet updated = walletRepository.findById(sampleWallet.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when withdrawing more than available balance")
    void shouldPreventOverdraft() {
        Wallet wallet = walletRepository.findById(sampleWallet.getId()).orElseThrow();

        assertThatThrownBy(() -> {
            wallet.withdraw(new BigDecimal("200.00"));
        }).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Saldo insuficiente");
    }
}
