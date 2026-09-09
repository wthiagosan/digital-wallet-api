package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Consulta o histórico completo de transações onde a carteira informada é origem ou destino,
     * ordenado da mais recente para a mais antiga (extrato).
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId ORDER BY t.createdAt DESC")
    List<Transaction> findAllByWalletIdOrderByCreatedAtDesc(@Param("walletId") Long walletId);

    /**
     * Consulta paginada do histórico de transações de uma carteira.
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId ORDER BY t.createdAt DESC")
    Page<Transaction> findAllByWalletIdOrderByCreatedAtDesc(@Param("walletId") Long walletId, Pageable pageable);
}
