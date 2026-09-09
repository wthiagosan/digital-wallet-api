package com.portfolio.wallet.exception;

public class IdempotencyConflictException extends BusinessException {

    private final String idempotencyKey;

    public IdempotencyConflictException(String idempotencyKey) {
        super("Uma requisição com a chave de idempotência '" + idempotencyKey + "' já está em processamento concorrente.");
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
