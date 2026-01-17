package com.ewallet.wallet_service.repository;

import com.ewallet.wallet_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromWalletIdOrToWalletIdOrderByTimestampDesc(
            Long fromWalletId,
            Long toWalletId
    );
}
