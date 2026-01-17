package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.response.TransactionResponse;
import com.ewallet.wallet_service.dto.response.WalletResponse;
import com.ewallet.wallet_service.entity.Transaction;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.exception.InsufficientBalanceException;
import com.ewallet.wallet_service.repository.TransactionRepository;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private BalanceWebSocketService balanceWebSocketService;
    @Mock private AuditLogService auditLogService;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        // Mock Security Context
        SecurityContextHolder.setContext(securityContext);
        
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal("1000.00"));
    }

    private void mockCurrentUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
    }

    @Test
    void getMyBalance_Success() {
        mockCurrentUser();

        WalletResponse response = walletService.getMyBalance();

        assertEquals(1L, response.getWalletId());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
    }

    @Test
    void transfer_Success() {
        mockCurrentUser();

        // Target Wallet
        User recipient = new User();
        recipient.setId(2L);
        Wallet targetWallet = new Wallet();
        targetWallet.setId(2L);
        targetWallet.setUser(recipient);
        targetWallet.setBalance(new BigDecimal("500.00"));

        when(walletRepository.findById(2L)).thenReturn(Optional.of(targetWallet));

        // Act
        walletService.transfer(2L, new BigDecimal("200.00"));

        // Assert Balance Updates
        assertEquals(new BigDecimal("800.00"), wallet.getBalance()); // 1000 - 200
        assertEquals(new BigDecimal("700.00"), targetWallet.getBalance()); // 500 + 200

        // Verify DB Saves
        verify(walletRepository).save(wallet);
        verify(walletRepository).save(targetWallet);
        verify(transactionRepository).save(any(Transaction.class));

        // Verify Websocket Updates
        verify(balanceWebSocketService).publishBalance(1L, new BigDecimal("800.00"));
        verify(balanceWebSocketService).publishBalance(2L, new BigDecimal("700.00"));
        
        // Verify Audit Logs
        verify(auditLogService, times(2)).log(any(), anyString(), eq("SUCCESS"), any(), any());
    }

    @Test
    void transfer_InsufficientBalance() {
        mockCurrentUser();

        User recipient = new User();
        Wallet targetWallet = new Wallet();
        targetWallet.setId(2L);
        targetWallet.setUser(recipient);
        targetWallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findById(2L)).thenReturn(Optional.of(targetWallet));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> 
            walletService.transfer(2L, new BigDecimal("5000.00"))
        );
        
        // Verify failure log for sender
        verify(auditLogService).log(eq(user), eq("TRANSFER"), eq("FAILURE"), any(), any());
    }

    @Test
    void getMyTransactionHistory_Success() {
        mockCurrentUser();

        Transaction tx = new Transaction();
        tx.setId(101L);
        tx.setFromWallet(wallet); // Debit
        tx.setToWallet(new Wallet());
        tx.getToWallet().setId(99L);
        tx.setAmount(BigDecimal.TEN);
        tx.setTimestamp(LocalDateTime.now());

        when(transactionRepository.findByFromWalletIdOrToWalletIdOrderByTimestampDesc(1L, 1L))
                .thenReturn(List.of(tx));

        List<TransactionResponse> history = walletService.getMyTransactionHistory();

        assertEquals(1, history.size());
        assertEquals("DEBIT", history.get(0).getType());
        assertEquals(new BigDecimal("10"), history.get(0).getAmount());
    }
}