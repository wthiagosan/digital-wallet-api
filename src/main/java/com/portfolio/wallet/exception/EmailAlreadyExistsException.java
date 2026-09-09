package com.portfolio.wallet.exception;

public class EmailAlreadyExistsException extends BusinessException {

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super(String.format("Já existe um usuário cadastrado com o e-mail: %s.", email));
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
