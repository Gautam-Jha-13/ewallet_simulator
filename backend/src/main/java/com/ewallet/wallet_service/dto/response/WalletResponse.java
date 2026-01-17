package com.ewallet.wallet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WalletResponse {

    private Long walletId;
    private BigDecimal balance;
}
