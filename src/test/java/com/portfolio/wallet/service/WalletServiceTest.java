package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.request.DepositRequest;
import com.portfolio.wallet.dto.response.StatementResponse;
import com.portfolio.wallet.dto.response.WalletResponse;
import com.portfolio.wallet.exception.WalletNotFoundException;
import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.User;
import com.portfolio.wallet.model.Wallet;
import com.portfolio.wallet.model.enums.TransactionStatus;
import com.portfolio.wallet.model.enums.TransactionType;
import com.portfolio.wallet.repository.TransactionRepository;
import com.portfolio.wallet.repository.WalletRepository;
import com.portfolio.wallet.service.impl.WalletServiceImpl;
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
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet sampleWallet;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(10L, "Katherine Johnson", "12345678900", "katherine@nasa.gov", LocalDateTime.now());
        sampleWallet = new Wallet(1L, sampleUser, new BigDecimal("200.00"), LocalDateTime.now(), LocalDateTime.now());
        sampleUser.setWallet(sampleWallet);
    }

    @Test
    @DisplayName("Should successfully deposit funds into wallet and record transaction")
    void shouldDepositSuccessfully() {
        // Arrange
        DepositRequest request = new DepositRequest(new BigDecimal("100.50"));
        when(walletRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sampleWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        WalletResponse response = walletService.deposit(1L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("300.50"));
        assertThat(sampleWallet.getBalance()).isEqualByComparingTo(new BigDecimal("300.50"));

        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(sampleWallet);
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when depositing into non-existent wallet")
    void shouldFailDepositWhenWalletNotFound() {
        DepositRequest request = new DepositRequest(new BigDecimal("50.00"));
        when(walletRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.deposit(999L, request))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("999");

        verify(transactionRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find wallet by ID successfully")
    void shouldFindById() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sampleWallet));

        WalletResponse response = walletService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Should find wallet by user ID successfully")
    void shouldFindByUserId() {
        when(walletRepository.findByUserId(10L)).thenReturn(Optional.of(sampleWallet));

        WalletResponse response = walletService.findByUserId(10L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should get complete wallet statement with ordered transactions")
    void shouldGetStatement() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sampleWallet));

        Transaction tx1 = new Transaction(101L, null, sampleWallet, new BigDecimal("200.00"),
                TransactionType.DEPOSIT, TransactionStatus.COMPLETED, LocalDateTime.now());
        Transaction tx2 = new Transaction(102L, sampleWallet, null, new BigDecimal("50.00"),
                TransactionType.TRANSFER, TransactionStatus.COMPLETED, LocalDateTime.now());

        when(transactionRepository.findAllByWalletIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(tx2, tx1));

        StatementResponse statement = walletService.getStatement(1L);

        assertThat(statement).isNotNull();
        assertThat(statement.walletId()).isEqualTo(1L);
        assertThat(statement.currentBalance()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(statement.transactions()).hasSize(2);
        assertThat(statement.transactions().get(0).id()).isEqualTo(102L);
        assertThat(statement.transactions().get(1).id()).isEqualTo(101L);
    }
}
