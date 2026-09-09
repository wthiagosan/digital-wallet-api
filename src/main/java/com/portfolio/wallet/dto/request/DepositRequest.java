package com.portfolio.wallet.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DepositRequest(
        @NotNull(message = "O valor do depósito é obrigatório")
        @Positive(message = "O valor do depósito deve ser estritamente maior que zero")
        @Digits(integer = 15, fraction = 2, message = "O valor deve conter no máximo 15 dígitos inteiros e 2 casas decimais")
        @DecimalMax(value = "1000000000.00", message = "O valor máximo permitido por depósito é R$ 1.000.000.000,00")
        BigDecimal amount
) {
}
