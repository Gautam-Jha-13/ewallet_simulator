package com.ewallet.wallet_service.service.impl;

import com.ewallet.wallet_service.dto.request.LoginRequest;
import com.ewallet.wallet_service.dto.request.UserCreateRequest;
import com.ewallet.wallet_service.dto.response.AuthResponse;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.exception.InvalidRequestException;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.security.JwtUtil;
import com.ewallet.wallet_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ewallet.wallet_service.service.AuditLogService; // Import this

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final BigDecimal MIN_INITIAL_BALANCE = BigDecimal.valueOf(1000);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService; // 1. Inject Service

    // =============================
    // CREATE USER + WALLET
    // =============================
    @Override
    @Transactional
    public void createUser(UserCreateRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidRequestException("Email already exists");
        }

        if (request.getInitialBalance().compareTo(MIN_INITIAL_BALANCE) < 0) {
            throw new InvalidRequestException(
                    "Minimum initial balance must be ₹" + MIN_INITIAL_BALANCE
            );
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(request.getInitialBalance());

        walletRepository.save(wallet);
    }

    // =============================
    // LOGIN
    // =============================
    @Override
    public AuthResponse login(LoginRequest request) {
        // Find user first to log their ID even if password fails
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        try {
            if (user == null) {
                throw new InvalidRequestException("Invalid email or password");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                auditLogService.log(user, "LOGIN", "FAILURE", null, null);
                throw new InvalidRequestException("Invalid email or password");
            }

            auditLogService.log(user, "LOGIN", "SUCCESS", null, null);

            String token = jwtUtil.generateToken(user.getEmail());
            return new AuthResponse(token);
        }

            catch (Exception e) {
            if (user != null && !(e instanceof InvalidRequestException)) {
                auditLogService.log(user, "LOGIN", "FAILURE", null, null);
            }
            throw e;
        }
    }
}
