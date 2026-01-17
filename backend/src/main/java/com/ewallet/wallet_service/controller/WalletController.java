package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.request.TransferRequest;
import com.ewallet.wallet_service.dto.response.TransactionResponse;
import com.ewallet.wallet_service.dto.response.WalletResponse;
import com.ewallet.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // =============================
    // GET MY BALANCE
    // =============================
    @GetMapping("/balance")
    public ResponseEntity<WalletResponse> getMyBalance() {
        return ResponseEntity.ok(walletService.getMyBalance());
    }

    // =============================
    // TRANSFER MONEY
    // =============================
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @Valid @RequestBody TransferRequest request
    ) {
        walletService.transfer(
                request.getToWalletId(),
                request.getAmount()
        );
        return ResponseEntity.ok("Transfer successful");
    }

    // =============================
    // MY TRANSACTION HISTORY
    // =============================
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getMyTransactions() {
        return ResponseEntity.ok(
                walletService.getMyTransactionHistory()
        );
    }
}
