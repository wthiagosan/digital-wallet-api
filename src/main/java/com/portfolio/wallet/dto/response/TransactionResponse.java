package com.portfolio.wallet.dto.response;

import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.enums.TransactionStatus;
import com.portfolio.wallet.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long sourceWalletId,
        Long targetWalletId,
        BigDecimal amount,
        TransactionType transactionType,
        TransactionStatus status,
        LocalDateTime createdAt
) {
    public static TransactionResponse fromEntity(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        Long sourceId = transaction.getSourceWallet() != null ? transaction.getSourceWallet().getId() : null;
        Long targetId = transaction.getTargetWallet() != null ? transaction.getTargetWallet().getId() : null;
        return new TransactionResponse(
                transaction.getId(),
                sourceId,
                targetId,
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}
