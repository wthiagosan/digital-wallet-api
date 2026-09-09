package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.User;
import com.portfolio.wallet.model.Wallet;
import com.portfolio.wallet.model.enums.TransactionStatus;
import com.portfolio.wallet.model.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
        User sender = new User("Sender User", "11111111111", "sender@wallet.com");
        entityManager.persistAndFlush(sender);
        senderWallet = new Wallet(sender, new BigDecimal("500.00"));
        entityManager.persistAndFlush(senderWallet);

        User receiver = new User("Receiver User", "22222222222", "receiver@wallet.com");
        entityManager.persistAndFlush(receiver);
        receiverWallet = new Wallet(receiver, new BigDecimal("100.00"));
        entityManager.persistAndFlush(receiverWallet);
    }

    @Test
    @DisplayName("Should save deposit transaction and retrieve it")
    void shouldSaveDepositTransaction() {
        Transaction deposit = Transaction.createDeposit(receiverWallet, new BigDecimal("250.00"));
        Transaction saved = transactionRepository.saveAndFlush(deposit);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSourceWallet()).isNull();
        assertThat(saved.getTargetWallet().getId()).isEqualTo(receiverWallet.getId());
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(saved.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should save transfer transaction between two wallets")
    void shouldSaveTransferTransaction() {
        Transaction transfer = Transaction.createTransfer(senderWallet, receiverWallet, new BigDecimal("75.50"));
        Transaction saved = transactionRepository.saveAndFlush(transfer);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSourceWallet().getId()).isEqualTo(senderWallet.getId());
        assertThat(saved.getTargetWallet().getId()).isEqualTo(receiverWallet.getId());
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("75.50"));
        assertThat(saved.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should retrieve wallet statement ordered by createdAt descending")
    void shouldFindAllByWalletIdOrderByCreatedAtDesc() throws InterruptedException {
        // Transação 1: Depósito na senderWallet
        Transaction tx1 = Transaction.createDeposit(senderWallet, new BigDecimal("100.00"));
        transactionRepository.saveAndFlush(tx1);

        Thread.sleep(10); // Garantir diferença de timestamp

        // Transação 2: Transferência de sender para receiver
        Transaction tx2 = Transaction.createTransfer(senderWallet, receiverWallet, new BigDecimal("50.00"));
        transactionRepository.saveAndFlush(tx2);

        Thread.sleep(10);

        // Transação 3: Depósito na receiverWallet (não envolve senderWallet)
        Transaction tx3 = Transaction.createDeposit(receiverWallet, new BigDecimal("200.00"));
        transactionRepository.saveAndFlush(tx3);

        // Consultar extrato da senderWallet
        List<Transaction> senderStatement = transactionRepository.findAllByWalletIdOrderByCreatedAtDesc(senderWallet.getId());

        assertThat(senderStatement).hasSize(2);
        assertThat(senderStatement.get(0).getId()).isEqualTo(tx2.getId()); // Mais recente primeiro
        assertThat(senderStatement.get(1).getId()).isEqualTo(tx1.getId());

        // Consultar extrato da receiverWallet
        List<Transaction> receiverStatement = transactionRepository.findAllByWalletIdOrderByCreatedAtDesc(receiverWallet.getId());

        assertThat(receiverStatement).hasSize(2);
        assertThat(receiverStatement.get(0).getId()).isEqualTo(tx3.getId()); // tx3 é o mais recente
        assertThat(receiverStatement.get(1).getId()).isEqualTo(tx2.getId()); // tx2 transferência recebida
    }
}
