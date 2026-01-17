package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.request.TransferRequest;
import com.ewallet.wallet_service.dto.response.WalletResponse;
import com.ewallet.wallet_service.security.JwtUtil;
import com.ewallet.wallet_service.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypassing Spring Security for unit testing logic
class WalletControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private WalletService walletService;
    @MockBean private JwtUtil jwtUtil;

    @Test
    void getMyBalance_Success() throws Exception {
        WalletResponse response = new WalletResponse(1L, new BigDecimal("100.00"));

        given(walletService.getMyBalance()).willReturn(response);

        mockMvc.perform(get("/api/wallet/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(1))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void transfer_Success() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setToWalletId(2L);
        request.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/wallet/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer successful"));

        verify(walletService).transfer(2L, new BigDecimal("50.00"));
    }
}