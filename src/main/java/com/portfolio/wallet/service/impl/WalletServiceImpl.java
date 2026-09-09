package com.portfolio.wallet.service.impl;

import com.portfolio.wallet.dto.request.DepositRequest;
import com.portfolio.wallet.dto.response.StatementResponse;
import com.portfolio.wallet.dto.response.TransactionResponse;
import com.portfolio.wallet.dto.response.WalletResponse;
import com.portfolio.wallet.exception.WalletNotFoundException;
import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.Wallet;
import com.portfolio.wallet.repository.TransactionRepository;
import com.portfolio.wallet.repository.WalletRepository;
import com.portfolio.wallet.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletServiceImpl(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public WalletResponse deposit(Long walletId, DepositRequest request) {
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        wallet.deposit(request.amount());

        Transaction depositTransaction = Transaction.createDeposit(wallet, request.amount());
        transactionRepository.save(depositTransaction);

        Wallet savedWallet = walletRepository.save(wallet);
        return WalletResponse.fromEntity(savedWallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse findById(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
        return WalletResponse.fromEntity(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse findByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Carteira vinculada ao usuário com ID " + userId + " não foi encontrada."));
        return WalletResponse.fromEntity(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public StatementResponse getStatement(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        List<TransactionResponse> transactions = transactionRepository
                .findAllByWalletIdOrderByCreatedAtDesc(walletId)
                .stream()
                .map(TransactionResponse::fromEntity)
                .toList();

        return new StatementResponse(wallet.getId(), wallet.getBalance(), transactions);
    }
}
