package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.request.LoginRequest;
import com.ewallet.wallet_service.dto.request.UserCreateRequest;
import com.ewallet.wallet_service.dto.response.AuthResponse;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.exception.InvalidRequestException;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.security.JwtUtil;
import com.ewallet.wallet_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    // --- CREATE USER TESTS ---

    @Test
    void createUser_Success() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setInitialBalance(new BigDecimal("1000"));

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");

        // Act
        userService.createUser(request);

        // Assert
        verify(userRepository).save(any(User.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createUser_EmailExists_ThrowsException() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("john@example.com");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(InvalidRequestException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_LowBalance_ThrowsException() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("john@example.com");
        request.setInitialBalance(new BigDecimal("500")); // Less than 1000

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class, () -> userService.createUser(request));
    }

    // --- LOGIN TESTS ---

    @Test
    void login_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setPassword("encodedPass");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("jwt-token");

        // Act
        AuthResponse response = userService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        
        // Verify Audit Log was called for SUCCESS
        verify(auditLogService).log(eq(user), eq("LOGIN"), eq("SUCCESS"), any(), any());
    }

    @Test
    void login_WrongPassword_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrongpass");

        User user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setPassword("encodedPass");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "encodedPass")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> userService.login(request));

        // Verify Audit Log was called for FAILURE
        verify(auditLogService).log(eq(user), eq("LOGIN"), eq("FAILURE"), any(), any());
    }
}