package com.portfolio.wallet.exception;

public class WalletNotFoundException extends BusinessException {

    public WalletNotFoundException(Long walletId) {
        super(String.format("Carteira com ID %d não foi encontrada.", walletId));
    }

    public WalletNotFoundException(String message) {
        super(message);
    }
}
