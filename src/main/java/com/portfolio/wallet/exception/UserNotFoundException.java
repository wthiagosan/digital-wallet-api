package com.portfolio.wallet.exception;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long userId) {
        super(String.format("Usuário com ID %d não foi encontrado.", userId));
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
