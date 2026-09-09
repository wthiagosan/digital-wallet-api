package com.portfolio.wallet.model;

import com.portfolio.wallet.model.enums.TransactionStatus;
import com.portfolio.wallet.model.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_wallet_id", nullable = false)
    private Wallet targetWallet;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {
    }

    public Transaction(Wallet sourceWallet, Wallet targetWallet, BigDecimal amount,
                       TransactionType transactionType, TransactionStatus status) {
        this.sourceWallet = sourceWallet;
        this.targetWallet = targetWallet;
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : null;
        this.transactionType = transactionType;
        this.status = status;
    }

    public Transaction(Long id, Wallet sourceWallet, Wallet targetWallet, BigDecimal amount,
                       TransactionType transactionType, TransactionStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.sourceWallet = sourceWallet;
        this.targetWallet = targetWallet;
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : null;
        this.transactionType = transactionType;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Factory method para criação de transação de depósito.
     */
    public static Transaction createDeposit(Wallet targetWallet, BigDecimal amount) {
        return new Transaction(null, targetWallet, amount, TransactionType.DEPOSIT, TransactionStatus.COMPLETED);
    }

    /**
     * Factory method para criação de transação de transferência P2P.
     */
    public static Transaction createTransfer(Wallet sourceWallet, Wallet targetWallet, BigDecimal amount) {
        return new Transaction(sourceWallet, targetWallet, amount, TransactionType.TRANSFER, TransactionStatus.COMPLETED);
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.amount != null) {
            this.amount = this.amount.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Wallet getSourceWallet() {
        return sourceWallet;
    }

    public void setSourceWallet(Wallet sourceWallet) {
        this.sourceWallet = sourceWallet;
    }

    public Wallet getTargetWallet() {
        return targetWallet;
    }

    public void setTargetWallet(Wallet targetWallet) {
        this.targetWallet = targetWallet;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : null;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", sourceWalletId=" + (sourceWallet != null ? sourceWallet.getId() : null) +
                ", targetWalletId=" + (targetWallet != null ? targetWallet.getId() : null) +
                ", amount=" + amount +
                ", transactionType=" + transactionType +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
