package com.portfolio.wallet.dto.response;

import com.portfolio.wallet.model.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(
        Long id,
        Long userId,
        BigDecimal balance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WalletResponse fromEntity(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        Long userId = wallet.getUser() != null ? wallet.getUser().getId() : null;
        return new WalletResponse(
                wallet.getId(),
                userId,
                wallet.getBalance(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}
