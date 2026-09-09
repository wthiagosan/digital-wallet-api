package com.portfolio.wallet.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record StatementResponse(
        Long walletId,
        BigDecimal currentBalance,
        List<TransactionResponse> transactions
) {
}
