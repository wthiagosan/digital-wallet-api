package com.portfolio.wallet.dto.response;

import com.portfolio.wallet.model.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String fullName,
        String documentNumber,
        String email,
        Long walletId,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        Long walletId = user.getWallet() != null ? user.getWallet().getId() : null;
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getDocumentNumber(),
                user.getEmail(),
                walletId,
                user.getCreatedAt()
        );
    }
}
