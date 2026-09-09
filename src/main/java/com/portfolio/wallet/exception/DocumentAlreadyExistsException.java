package com.portfolio.wallet.exception;

public class DocumentAlreadyExistsException extends BusinessException {

    private final String documentNumber;

    public DocumentAlreadyExistsException(String documentNumber) {
        super(String.format("Já existe um usuário cadastrado com o CPF/CNPJ: %s.", documentNumber));
        this.documentNumber = documentNumber;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }
}
