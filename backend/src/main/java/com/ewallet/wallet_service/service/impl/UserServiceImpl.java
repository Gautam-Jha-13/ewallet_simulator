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
import com.ewallet.wallet_service.service.AuditLogService;
import com.ewallet.wallet_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserServiceImpl.class);

    private static final BigDecimal MIN_INITIAL_BALANCE =
            BigDecimal.valueOf(1000);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    // =============================
    // CREATE USER + WALLET
    // =============================
    @Override
    @Transactional
    public void createUser(UserCreateRequest request) {

        log.info("User registration attempt for email={}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email already exists [{}]",
                    request.getEmail());
            throw new InvalidRequestException("Email already exists");
        }

        if (request.getInitialBalance()
                .compareTo(MIN_INITIAL_BALANCE) < 0) {

            log.warn(
                "Registration failed for email={} due to low initial balance: {}",
                request.getEmail(),
                request.getInitialBalance()
            );

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

        log.info(
            "User created successfully. userId={}, walletBalance={}",
            user.getId(),
            wallet.getBalance()
        );
    }

    // =============================
    // LOGIN
    // =============================
    @Override
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for email={}", request.getEmail());

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        try {
            if (user == null) {
                log.warn("Login failed: Invalid email [{}]",
                        request.getEmail());
                throw new InvalidRequestException(
                        "Invalid email or password"
                );
            }

            if (!passwordEncoder.matches(
                    request.getPassword(),
                    user.getPassword())) {

                log.warn("Login failed: Wrong password for userId={}",
                        user.getId());

                auditLogService.log(
                        user,
                        "LOGIN",
                        "FAILURE",
                        null,
                        null
                );

                throw new InvalidRequestException(
                        "Invalid email or password"
                );
            }

            auditLogService.log(
                    user,
                    "LOGIN",
                    "SUCCESS",
                    null,
                    null
            );

            log.info("Login successful for userId={}", user.getId());

            String token = jwtUtil.generateToken(user.getEmail());
            return new AuthResponse(token);

        } catch (Exception e) {

            if (user != null &&
                !(e instanceof InvalidRequestException)) {

                log.error(
                    "Unexpected login error for userId={}",
                    user.getId(),
                    e
                );

                auditLogService.log(
                        user,
                        "LOGIN",
                        "FAILURE",
                        null,
                        null
                );
            }

            throw e;
        }
    }
}
