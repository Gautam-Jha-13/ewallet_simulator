package com.ewallet.wallet_service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull
    private Long toWalletId;

    @NotNull
    @Positive
    private BigDecimal amount;
}
