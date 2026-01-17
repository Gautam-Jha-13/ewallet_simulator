package com.ewallet.wallet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TransactionResponse {

    private Long transactionId;

    // DEBIT or CREDIT (from current user's perspective)
    private String type;

    private BigDecimal amount;

    private Long counterpartyWalletId;

    private LocalDateTime timestamp;
}
