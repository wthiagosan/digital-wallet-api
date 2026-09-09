package com.portfolio.wallet.exception;

public class SameWalletTransferException extends BusinessException {

    public SameWalletTransferException(Long walletId) {
        super(String.format("Transferência inválida: as carteiras de origem e destino são as mesmas (ID: %d).", walletId));
    }

    public SameWalletTransferException() {
        super("A carteira de origem e a carteira de destino não podem ser iguais.");
    }
}
