package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.request.DepositRequest;
import com.portfolio.wallet.dto.response.StatementResponse;
import com.portfolio.wallet.dto.response.WalletResponse;

public interface WalletService {

    WalletResponse deposit(Long walletId, DepositRequest request);

    WalletResponse findById(Long id);

    WalletResponse findByUserId(Long userId);

    StatementResponse getStatement(Long walletId);
}
