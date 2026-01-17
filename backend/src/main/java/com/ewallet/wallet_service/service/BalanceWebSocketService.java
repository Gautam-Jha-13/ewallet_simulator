package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.response.BalanceUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishBalance(Long walletId, BigDecimal balance) {
        BalanceUpdateResponse response =
                new BalanceUpdateResponse(walletId, balance);

        messagingTemplate.convertAndSend(
                "/topic/wallet/" + walletId,
                response
        );
    }
}
