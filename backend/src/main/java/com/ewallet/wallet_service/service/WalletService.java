package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.response.TransactionResponse;
import com.ewallet.wallet_service.dto.response.WalletResponse;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {

    WalletResponse getMyBalance();

    void transfer(Long toWalletId, BigDecimal amount);

    List<TransactionResponse> getMyTransactionHistory();
}
