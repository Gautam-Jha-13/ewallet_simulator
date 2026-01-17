package com.ewallet.wallet_service.service.impl;

import com.ewallet.wallet_service.dto.response.TransactionResponse;
import com.ewallet.wallet_service.dto.response.WalletResponse;
import com.ewallet.wallet_service.entity.Transaction;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.exception.InsufficientBalanceException;
import com.ewallet.wallet_service.exception.ResourceNotFoundException;
import com.ewallet.wallet_service.repository.TransactionRepository;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.service.BalanceWebSocketService;
import com.ewallet.wallet_service.service.WalletService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.ewallet.wallet_service.service.AuditLogService; 

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BalanceWebSocketService balanceWebSocketService;
    private final AuditLogService auditLogService; 

    public WalletServiceImpl(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            BalanceWebSocketService balanceWebSocketService,
            AuditLogService auditLogService 
    ) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.balanceWebSocketService = balanceWebSocketService;
        this.auditLogService = auditLogService;
    }

    // =============================
    // HELPER: CURRENT USER WALLET
    // =============================
    private Wallet getCurrentUserWallet() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        return walletRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found")
                );
    }

    // =============================
    // GET MY BALANCE
    // =============================
    @Override
    public WalletResponse getMyBalance() {

        Wallet wallet = getCurrentUserWallet();

        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance()
        );
    }

    // =============================
    // TRANSFER MONEY (ACID + AUTH)
    // =============================
    @Override
    public void transfer(Long toWalletId, BigDecimal amount) {

        Wallet fromWallet = getCurrentUserWallet();

        Wallet toWallet = walletRepository.findById(toWalletId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Target wallet not found")
                );
        BigDecimal senderOldBal = fromWallet.getBalance();
        BigDecimal receiverOldBal = toWallet.getBalance();

        try{

        if (fromWallet.getId().equals(toWalletId)) {
            throw new IllegalArgumentException("Cannot transfer to same wallet");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // ACID protected
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        Transaction tx = new Transaction();
        tx.setFromWallet(fromWallet);
        tx.setToWallet(toWallet);
        tx.setAmount(amount);
        tx.setTimestamp(LocalDateTime.now());

        transactionRepository.save(tx);

        // --- AUDIT LOGGING START ---
            
            // 1. Log Sender (TRANSFER)
            auditLogService.log(
                fromWallet.getUser(), 
                "TRANSFER", 
                "SUCCESS", 
                senderOldBal, 
                fromWallet.getBalance()
            );

            // 2. Log Receiver (RECEIVE)
            auditLogService.log(
                toWallet.getUser(), 
                "RECEIVE", 
                "SUCCESS", 
                receiverOldBal, 
                toWallet.getBalance()
            );
            // --- AUDIT LOGGING END ---

        // 🔥 REAL-TIME BALANCE UPDATES
        balanceWebSocketService.publishBalance(
                fromWallet.getId(),
                fromWallet.getBalance()
        );

        balanceWebSocketService.publishBalance(
                toWallet.getId(),
                toWallet.getBalance()
        );
    }catch (Exception e) {
            // Log Failure for Sender
            auditLogService.log(
                fromWallet.getUser(), 
                "TRANSFER", 
                "FAILURE", 
                senderOldBal, 
                senderOldBal
            );
            throw e; // Rethrow to rollback DB transaction
        }
}

    // =============================
    // MY TRANSACTION HISTORY
    // =============================
    @Override
    public List<TransactionResponse> getMyTransactionHistory() {

        Wallet wallet = getCurrentUserWallet();

        List<Transaction> transactions =
                transactionRepository
                        .findByFromWalletIdOrToWalletIdOrderByTimestampDesc(
                                wallet.getId(),
                                wallet.getId()
                        );

        return transactions.stream().map(tx -> {

            boolean isDebit =
                    tx.getFromWallet().getId().equals(wallet.getId());

            return new TransactionResponse(
                    tx.getId(),
                    isDebit ? "DEBIT" : "CREDIT",
                    tx.getAmount(),
                    isDebit
                            ? tx.getToWallet().getId()
                            : tx.getFromWallet().getId(),
                    tx.getTimestamp()
            );
        }).toList();
    }
}
