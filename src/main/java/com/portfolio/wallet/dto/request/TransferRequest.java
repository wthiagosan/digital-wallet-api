package com.portfolio.wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "O ID da carteira de origem é obrigatório")
        Long sourceWalletId,

        @NotNull(message = "O ID da carteira de destino é obrigatório")
        Long targetWalletId,

        @NotNull(message = "O valor da transferência é obrigatório")
        @Positive(message = "O valor da transferência deve ser estritamente maior que zero")
        BigDecimal amount
) {
}
