package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.request.LoginRequest;
import com.ewallet.wallet_service.dto.response.AuthResponse;
import com.ewallet.wallet_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(userService.login(request));
    }
}
