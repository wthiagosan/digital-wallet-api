package com.portfolio.wallet.service.impl;

import com.portfolio.wallet.dto.request.TransferRequest;
import com.portfolio.wallet.dto.response.TransferResponse;
import com.portfolio.wallet.exception.InsufficientBalanceException;
import com.portfolio.wallet.exception.SameWalletTransferException;
import com.portfolio.wallet.exception.WalletNotFoundException;
import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.Wallet;
import com.portfolio.wallet.repository.TransactionRepository;
import com.portfolio.wallet.repository.WalletRepository;
import com.portfolio.wallet.service.TransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferServiceImpl implements TransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransferServiceImpl(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        // 1. Validação de auto-transferência (Self-transfer check)
        if (request.sourceWalletId().equals(request.targetWalletId())) {
            throw new SameWalletTransferException(request.sourceWalletId());
        }

        // 2. Validação defensiva do valor
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero.");
        }

        // 3. Ordenação determinística de lock para prevenção de Deadlock em concorrência
        // Sempre adquire o lock da carteira com menor ID primeiro
        Long firstLockId = Math.min(request.sourceWalletId(), request.targetWalletId());
        Long secondLockId = Math.max(request.sourceWalletId(), request.targetWalletId());

        Wallet firstLocked = walletRepository.findByIdWithLock(firstLockId)
                .orElseThrow(() -> new WalletNotFoundException(firstLockId));

        Wallet secondLocked = walletRepository.findByIdWithLock(secondLockId)
                .orElseThrow(() -> new WalletNotFoundException(secondLockId));

        Wallet sourceWallet = request.sourceWalletId().equals(firstLockId) ? firstLocked : secondLocked;
        Wallet targetWallet = request.targetWalletId().equals(firstLockId) ? firstLocked : secondLocked;

        // 4. Validação de saldo suficiente na carteira de origem
        if (!sourceWallet.hasSufficientBalance(request.amount())) {
            throw new InsufficientBalanceException(sourceWallet.getId(), sourceWallet.getBalance(), request.amount());
        }

        // 5. Movimentação atômica dos saldos utilizando Rich Domain Model
        sourceWallet.withdraw(request.amount());
        targetWallet.deposit(request.amount());

        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);

        // 6. Registro de auditoria da transação financeira
        Transaction transaction = Transaction.createTransfer(sourceWallet, targetWallet, request.amount());
        Transaction savedTx = transactionRepository.save(transaction);

        return new TransferResponse(
                savedTx.getId(),
                sourceWallet.getId(),
                targetWallet.getId(),
                savedTx.getAmount(),
                savedTx.getStatus(),
                savedTx.getCreatedAt()
        );
    }
}
