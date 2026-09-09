package com.portfolio.wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DepositRequest(
        @NotNull(message = "O valor do depósito é obrigatório")
        @Positive(message = "O valor do depósito deve ser estritamente maior que zero")
        BigDecimal amount
) {
}
