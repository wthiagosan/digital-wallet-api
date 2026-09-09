package com.portfolio.wallet.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BusinessException {

    private final Long walletId;
    private final BigDecimal currentBalance;
    private final BigDecimal requiredAmount;

    public InsufficientBalanceException(Long walletId, BigDecimal currentBalance, BigDecimal requiredAmount) {
        super(String.format("A carteira de ID %d possui saldo de R$ %s, mas a operação requer R$ %s.",
                walletId, currentBalance, requiredAmount));
        this.walletId = walletId;
        this.currentBalance = currentBalance;
        this.requiredAmount = requiredAmount;
    }

    public Long getWalletId() {
        return walletId;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getRequiredAmount() {
        return requiredAmount;
    }
}
