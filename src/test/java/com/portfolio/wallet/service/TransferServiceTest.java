package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.request.TransferRequest;
import com.portfolio.wallet.dto.response.TransferResponse;
import com.portfolio.wallet.exception.InsufficientBalanceException;
import com.portfolio.wallet.exception.SameWalletTransferException;
import com.portfolio.wallet.exception.WalletNotFoundException;
import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.User;
import com.portfolio.wallet.model.Wallet;
import com.portfolio.wallet.model.enums.TransactionStatus;
import com.portfolio.wallet.model.enums.TransactionType;
import com.portfolio.wallet.repository.TransactionRepository;
import com.portfolio.wallet.repository.WalletRepository;
import com.portfolio.wallet.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private Wallet sourceWallet;
    private Wallet targetWallet;

    @BeforeEach
    void setUp() {
        User user1 = new User(1L, "Source User", "11111111111", "source@mail.com", LocalDateTime.now());
        sourceWallet = new Wallet(1L, user1, new BigDecimal("100.00"), LocalDateTime.now(), LocalDateTime.now());

        User user2 = new User(2L, "Target User", "22222222222", "target@mail.com", LocalDateTime.now());
        targetWallet = new Wallet(2L, user2, new BigDecimal("50.00"), LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Should successfully transfer funds between two valid wallets")
    void shouldTransferSuccessfully() {
        // Arrange
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("30.00"));

        when(walletRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByIdWithLock(2L)).thenReturn(Optional.of(targetWallet));

        Transaction savedTx = new Transaction(99L, sourceWallet, targetWallet, new BigDecimal("30.00"),
                TransactionType.TRANSFER, TransactionStatus.COMPLETED, LocalDateTime.now());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        // Act
        TransferResponse response = transferService.transfer(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.transactionId()).isEqualTo(99L);
        assertThat(response.sourceWalletId()).isEqualTo(1L);
        assertThat(response.targetWalletId()).isEqualTo(2L);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);

        // Verifica que os saldos foram alterados no domínio
        assertThat(sourceWallet.getBalance()).isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(targetWallet.getBalance()).isEqualByComparingTo(new BigDecimal("80.00"));

        verify(walletRepository).save(sourceWallet);
        verify(walletRepository).save(targetWallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when source wallet does not have enough funds")
    void shouldFailWhenInsufficientBalance() {
        // Arrange
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("150.00"));

        when(walletRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByIdWithLock(2L)).thenReturn(Optional.of(targetWallet));

        // Act & Assert
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("saldo de R$ 100.00, mas a operação requer R$ 150.00");

        // Saldos devem permanecer intactos
        assertThat(sourceWallet.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(targetWallet.getBalance()).isEqualByComparingTo(new BigDecimal("50.00"));

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw SameWalletTransferException when source and target wallets are the same")
    void shouldFailWhenSameWalletTransfer() {
        // Arrange
        TransferRequest request = new TransferRequest(1L, 1L, new BigDecimal("20.00"));

        // Act & Assert
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(SameWalletTransferException.class)
                .hasMessageContaining("as carteiras de origem e destino são as mesmas");

        verify(walletRepository, never()).findByIdWithLock(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when source wallet does not exist")
    void shouldFailWhenSourceWalletNotFound() {
        // Arrange
        TransferRequest request = new TransferRequest(999L, 2L, new BigDecimal("10.00"));
        // O lock ordena 2L primeiro, depois 999L
        when(walletRepository.findByIdWithLock(2L)).thenReturn(Optional.of(targetWallet));
        when(walletRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("999");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when target wallet does not exist")
    void shouldFailWhenTargetWalletNotFound() {
        // Arrange
        TransferRequest request = new TransferRequest(1L, 888L, new BigDecimal("10.00"));
        when(walletRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByIdWithLock(888L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("888");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when transfer amount is null or non-positive")
    void shouldFailWhenAmountNonPositive() {
        TransferRequest zeroRequest = new TransferRequest(1L, 2L, BigDecimal.ZERO);
        TransferRequest negativeRequest = new TransferRequest(1L, 2L, new BigDecimal("-10.00"));

        assertThatThrownBy(() -> transferService.transfer(zeroRequest))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> transferService.transfer(negativeRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(walletRepository, never()).findByIdWithLock(any());
    }
}
