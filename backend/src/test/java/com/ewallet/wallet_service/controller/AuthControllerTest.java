package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.request.LoginRequest;
import com.ewallet.wallet_service.dto.response.AuthResponse;
import com.ewallet.wallet_service.exception.GlobalExceptionHandler; // Import this
import com.ewallet.wallet_service.security.JwtUtil;
import com.ewallet.wallet_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan; // Import this
import org.springframework.context.annotation.FilterType;     // Import this
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Update the annotation to EXCLUDE the GlobalExceptionHandler
@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, 
        classes = GlobalExceptionHandler.class
    )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean 
    private JwtUtil jwtUtil; 

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        AuthResponse authResponse = new AuthResponse("mock-token");

        when(userService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"));
    }

    @Test
    void login_InvalidRequest_Returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        // Missing email and password intentionally

        // Since GlobalExceptionHandler is excluded, Spring's default handler 
        // catches the validation error and returns 400 Bad Request
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}