package com.portfolio.wallet.dto.response;

import com.portfolio.wallet.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long transactionId,
        Long sourceWalletId,
        Long targetWalletId,
        BigDecimal amount,
        TransactionStatus status,
        LocalDateTime timestamp
) {
}
