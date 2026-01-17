package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.request.UserCreateRequest;
import com.ewallet.wallet_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupController {

    private final UserService userService;

    /**
     * Create user + wallet (ONE TIME SETUP)
     */
    @PostMapping("/user")
    public ResponseEntity<String> createUser(
            @Valid @RequestBody UserCreateRequest request
    ) {
        userService.createUser(request);
        return ResponseEntity.ok("User and wallet created successfully");
    }
}
