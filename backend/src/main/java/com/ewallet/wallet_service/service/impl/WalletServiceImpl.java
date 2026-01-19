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
import com.ewallet.wallet_service.service.AuditLogService;
import com.ewallet.wallet_service.service.BalanceWebSocketService;
import com.ewallet.wallet_service.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    private static final Logger log =
            LoggerFactory.getLogger(WalletServiceImpl.class);

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
                .orElseThrow(() -> {
                    log.error("Authenticated user not found: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        return walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.error("Wallet not found for userId={}", user.getId());
                    return new ResourceNotFoundException("Wallet not found");
                });
    }

    // =============================
    // GET MY BALANCE
    // =============================
    @Override
    public WalletResponse getMyBalance() {

        Wallet wallet = getCurrentUserWallet();

        log.info("Balance fetched for userId={}, balance={}",
                wallet.getUser().getId(),
                wallet.getBalance());

        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }

    // =============================
    // TRANSFER MONEY (ACID)
    // =============================
    @Override
    public void transfer(Long toWalletId, BigDecimal amount) {

        Wallet fromWallet = getCurrentUserWallet();
        Wallet toWallet = walletRepository.findById(toWalletId)
                .orElseThrow(() -> {
                    log.warn("Transfer failed: target wallet {} not found",
                            toWalletId);
                    return new ResourceNotFoundException(
                            "Target wallet not found");
                });

        BigDecimal senderOldBal = fromWallet.getBalance();
        BigDecimal receiverOldBal = toWallet.getBalance();

        log.info(
            "Transfer initiated from walletId={} to walletId={}, amount={}",
            fromWallet.getId(),
            toWalletId,
            amount
        );

        try {
            if (fromWallet.getId().equals(toWalletId)) {
                log.warn("Transfer failed: same source and target wallet");
                throw new IllegalArgumentException(
                        "Cannot transfer to same wallet");
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Transfer failed: invalid amount {}", amount);
                throw new IllegalArgumentException(
                        "Transfer amount must be positive");
            }

            if (fromWallet.getBalance().compareTo(amount) < 0) {
                log.warn(
                    "Transfer failed: insufficient balance. walletId={}, balance={}, amount={}",
                    fromWallet.getId(),
                    fromWallet.getBalance(),
                    amount
                );
                throw new InsufficientBalanceException(
                        "Insufficient balance");
            }

            // ACID section
            fromWallet.setBalance(senderOldBal.subtract(amount));
            toWallet.setBalance(receiverOldBal.add(amount));

            walletRepository.save(fromWallet);
            walletRepository.save(toWallet);

            Transaction tx = new Transaction();
            tx.setFromWallet(fromWallet);
            tx.setToWallet(toWallet);
            tx.setAmount(amount);
            tx.setTimestamp(LocalDateTime.now());

            transactionRepository.save(tx);

            // AUDIT LOGS
            auditLogService.log(
                    fromWallet.getUser(),
                    "TRANSFER",
                    "SUCCESS",
                    senderOldBal,
                    fromWallet.getBalance()
            );

            auditLogService.log(
                    toWallet.getUser(),
                    "RECEIVE",
                    "SUCCESS",
                    receiverOldBal,
                    toWallet.getBalance()
            );

            // REAL-TIME UPDATES
            balanceWebSocketService.publishBalance(
                    fromWallet.getId(),
                    fromWallet.getBalance()
            );

            balanceWebSocketService.publishBalance(
                    toWallet.getId(),
                    toWallet.getBalance()
            );

            log.info(
                "Transfer successful. fromWallet={}, toWallet={}, amount={}",
                fromWallet.getId(),
                toWallet.getId(),
                amount
            );

        } catch (Exception e) {

            log.error(
                "Transfer failed. fromWallet={}, toWallet={}, amount={}",
                fromWallet.getId(),
                toWalletId,
                amount,
                e
            );

            auditLogService.log(
                    fromWallet.getUser(),
                    "TRANSFER",
                    "FAILURE",
                    senderOldBal,
                    senderOldBal
            );

            throw e; // rollback
        }
    }

    // =============================
    // TRANSACTION HISTORY
    // =============================
    @Override
    public List<TransactionResponse> getMyTransactionHistory() {

        Wallet wallet = getCurrentUserWallet();

        log.info("Fetching transaction history for walletId={}",
                wallet.getId());

        List<Transaction> transactions =
                transactionRepository
                        .findByFromWalletIdOrToWalletIdOrderByTimestampDesc(
                                wallet.getId(),
                                wallet.getId()
                        );

        log.info(
            "Transaction history fetched for walletId={}, count={}",
            wallet.getId(),
            transactions.size()
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
