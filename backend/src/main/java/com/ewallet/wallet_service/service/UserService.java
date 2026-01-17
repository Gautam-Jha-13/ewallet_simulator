package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.request.LoginRequest;
import com.ewallet.wallet_service.dto.request.UserCreateRequest;
import com.ewallet.wallet_service.dto.response.AuthResponse;

public interface UserService {

    void createUser(UserCreateRequest request);

    AuthResponse login(LoginRequest request);
}
