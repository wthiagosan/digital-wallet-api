package com.portfolio.wallet.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/transfers");
    }

    @Test
    @DisplayName("Should format InsufficientBalanceException as RFC 7807 with 422 status")
    void shouldHandleInsufficientBalanceException() {
        var ex = new InsufficientBalanceException(1L, new BigDecimal("50.00"), new BigDecimal("100.00"));

        ProblemDetail pd = exceptionHandler.handleInsufficientBalanceException(ex, request);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(pd.getTitle()).isEqualTo("Saldo Insuficiente");
        assertThat(pd.getDetail()).contains("A carteira de ID 1 possui saldo de R$ 50.00, mas a operação requer R$ 100.00");
        assertThat(pd.getType().toString()).isEqualTo("https://api.wallet.com/errors/insufficient-funds");
        assertThat(pd.getInstance().toString()).isEqualTo("/api/v1/transfers");
        assertThat(pd.getProperties()).containsEntry("code", "INSUFFICIENT_FUNDS");
        assertThat(pd.getProperties()).containsEntry("walletId", 1L);
        assertThat(pd.getProperties()).containsEntry("currentBalance", new BigDecimal("50.00"));
        assertThat(pd.getProperties()).containsEntry("requiredAmount", new BigDecimal("100.00"));
        assertThat(pd.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Should format WalletNotFoundException as RFC 7807 with 404 status")
    void shouldHandleWalletNotFoundException() {
        var ex = new WalletNotFoundException(99L);

        ProblemDetail pd = exceptionHandler.handleWalletNotFoundException(ex, request);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(pd.getTitle()).isEqualTo("Carteira Não Encontrada");
        assertThat(pd.getDetail()).isEqualTo("Carteira com ID 99 não foi encontrada.");
        assertThat(pd.getProperties()).containsEntry("code", "WALLET_NOT_FOUND");
    }

    @Test
    @DisplayName("Should format UserNotFoundException as RFC 7807 with 404 status")
    void shouldHandleUserNotFoundException() {
        var ex = new UserNotFoundException(42L);

        ProblemDetail pd = exceptionHandler.handleUserNotFoundException(ex, request);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(pd.getTitle()).isEqualTo("Usuário Não Encontrado");
        assertThat(pd.getProperties()).containsEntry("code", "USER_NOT_FOUND");
    }

    @Test
    @DisplayName("Should format SameWalletTransferException as RFC 7807 with 400 status")
    void shouldHandleSameWalletTransferException() {
        var ex = new SameWalletTransferException(5L);

        ProblemDetail pd = exceptionHandler.handleSameWalletTransferException(ex, request);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(pd.getTitle()).isEqualTo("Transferência Para Mesma Carteira Proibida");
        assertThat(pd.getProperties()).containsEntry("code", "SELF_TRANSFER_FORBIDDEN");
    }

    @Test
    @DisplayName("Should format DocumentAlreadyExistsException as RFC 7807 with 409 status")
    void shouldHandleDocumentAlreadyExistsException() {
        var ex = new DocumentAlreadyExistsException("12345678901");

        ProblemDetail pd = exceptionHandler.handleDocumentAlreadyExistsException(ex, request);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(pd.getTitle()).isEqualTo("Documento Já Cadastrado");
        assertThat(pd.getProperties()).containsEntry("code", "DOCUMENT_ALREADY_EXISTS");
        assertThat(pd.getProperties()).containsEntry("documentNumber", "12345678901");
    }

    @Test
    @DisplayName("Should format EmailAlreadyExistsException as RFC 7807 with 409 status")
    void shouldHandleEmailAlreadyExistsException() {
        var ex = new EmailAlreadyExistsException("user@wallet.com");

        ProblemDetail pd = exceptionHandler.handleEmailAlreadyExistsException(ex, request);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(pd.getTitle()).isEqualTo("E-mail Já Cadastrado");
        assertThat(pd.getProperties()).containsEntry("code", "EMAIL_ALREADY_EXISTS");
        assertThat(pd.getProperties()).containsEntry("email", "user@wallet.com");
    }

    @Test
    @DisplayName("Should format unhandled Exception as RFC 7807 with 500 status")
    void shouldHandleGenericException() {
        var ex = new RuntimeException("Falha catastrófica inesperada");

        ProblemDetail pd = exceptionHandler.handleGenericException(ex, request);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(pd.getTitle()).isEqualTo("Erro Interno do Servidor");
        assertThat(pd.getProperties()).containsEntry("code", "INTERNAL_SERVER_ERROR");
    }
}
