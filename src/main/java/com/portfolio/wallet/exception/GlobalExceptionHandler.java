package com.portfolio.wallet.exception;

import com.portfolio.wallet.security.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_ERROR_URI = "https://api.wallet.com/errors/";

    private ProblemDetail buildProblemDetail(HttpStatus status, String detail, String title, String errorType, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create(BASE_ERROR_URI + errorType));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());

        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        if (correlationId != null) {
            problemDetail.setProperty("correlationId", correlationId);
        }
        return problemDetail;
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ProblemDetail handleInsufficientBalanceException(InsufficientBalanceException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(),
                "Saldo Insuficiente", "insufficient-funds", request);
        pd.setProperty("code", "INSUFFICIENT_FUNDS");
        pd.setProperty("walletId", ex.getWalletId());
        pd.setProperty("currentBalance", ex.getCurrentBalance());
        pd.setProperty("requiredAmount", ex.getRequiredAmount());
        return pd;
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ProblemDetail handleWalletNotFoundException(WalletNotFoundException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(),
                "Carteira Não Encontrada", "wallet-not-found", request);
        pd.setProperty("code", "WALLET_NOT_FOUND");
        return pd;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(),
                "Usuário Não Encontrado", "user-not-found", request);
        pd.setProperty("code", "USER_NOT_FOUND");
        return pd;
    }

    @ExceptionHandler(SameWalletTransferException.class)
    public ProblemDetail handleSameWalletTransferException(SameWalletTransferException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(),
                "Transferência Para Mesma Carteira Proibida", "self-transfer-forbidden", request);
        pd.setProperty("code", "SELF_TRANSFER_FORBIDDEN");
        return pd;
    }

    @ExceptionHandler(DocumentAlreadyExistsException.class)
    public ProblemDetail handleDocumentAlreadyExistsException(DocumentAlreadyExistsException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.CONFLICT, ex.getMessage(),
                "Documento Já Cadastrado", "document-already-exists", request);
        pd.setProperty("code", "DOCUMENT_ALREADY_EXISTS");
        pd.setProperty("documentNumber", ex.getDocumentNumber());
        return pd;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.CONFLICT, ex.getMessage(),
                "E-mail Já Cadastrado", "email-already-exists", request);
        pd.setProperty("code", "EMAIL_ALREADY_EXISTS");
        pd.setProperty("email", ex.getEmail());
        return pd;
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ProblemDetail handleIdempotencyConflictException(IdempotencyConflictException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.CONFLICT, ex.getMessage(),
                "Conflito de Idempotência", "idempotency-conflict", request);
        pd.setProperty("code", "IDEMPOTENCY_CONFLICT");
        pd.setProperty("idempotencyKey", ex.getIdempotencyKey());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.BAD_REQUEST,
                "Um ou mais campos de entrada contêm valores inválidos.",
                "Parâmetros de Entrada Inválidos", "invalid-input-parameters", request);
        pd.setProperty("code", "INVALID_INPUT_PARAMETERS");

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        pd.setProperty("invalidParams", fieldErrors);
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.BAD_REQUEST,
                "Um ou mais parâmetros da requisição violam as restrições de validação.",
                "Violação de Restrição", "constraint-violation", request);
        pd.setProperty("code", "CONSTRAINT_VIOLATION");

        Map<String, String> violations = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            violations.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        pd.setProperty("invalidParams", violations);
        return pd;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String detail = String.format("O parâmetro '%s' recebeu um valor incompatível: '%s'.", ex.getName(), ex.getValue());
        ProblemDetail pd = buildProblemDetail(HttpStatus.BAD_REQUEST, detail,
                "Tipo de Parâmetro Inválido", "type-mismatch", request);
        pd.setProperty("code", "TYPE_MISMATCH");
        pd.setProperty("parameter", ex.getName());
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.BAD_REQUEST,
                "Corpo da requisição ausente ou em formato JSON malformado.",
                "Corpo da Requisição Inválido", "malformed-request-body", request);
        pd.setProperty("code", "MALFORMED_REQUEST_BODY");
        return pd;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String detail = String.format("O método HTTP '%s' não é suportado para este endpoint.", ex.getMethod());
        ProblemDetail pd = buildProblemDetail(HttpStatus.METHOD_NOT_ALLOWED, detail,
                "Método HTTP Não Suportado", "method-not-allowed", request);
        pd.setProperty("code", "METHOD_NOT_ALLOWED");
        return pd;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.NOT_FOUND,
                "O recurso solicitado não foi encontrado.",
                "Recurso Não Encontrado", "resource-not-found", request);
        pd.setProperty("code", "RESOURCE_NOT_FOUND");
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        ProblemDetail pd = buildProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(),
                "Argumento Inválido", "illegal-argument", request);
        pd.setProperty("code", "ILLEGAL_ARGUMENT");
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        log.error("Internal server error [correlationId={}]: {}", correlationId, ex.getMessage(), ex);

        ProblemDetail pd = buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado no servidor. Por favor, tente novamente mais tarde.",
                "Erro Interno do Servidor", "internal-server-error", request);
        pd.setProperty("code", "INTERNAL_SERVER_ERROR");
        return pd;
    }
}
