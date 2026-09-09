package com.portfolio.wallet.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "O ID da carteira de origem é obrigatório")
        @Positive(message = "O ID da carteira de origem deve ser maior que zero")
        Long sourceWalletId,

        @NotNull(message = "O ID da carteira de destino é obrigatório")
        @Positive(message = "O ID da carteira de destino deve ser maior que zero")
        Long targetWalletId,

        @NotNull(message = "O valor da transferência é obrigatório")
        @Positive(message = "O valor da transferência deve ser estritamente maior que zero")
        @Digits(integer = 15, fraction = 2, message = "O valor deve conter no máximo 15 dígitos inteiros e 2 casas decimais")
        @DecimalMax(value = "1000000000.00", message = "O valor máximo permitido por transferência é R$ 1.000.000.000,00")
        BigDecimal amount
) {
}
