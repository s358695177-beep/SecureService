package com.sunjintong.secureservice.service.auth;

import com.sunjintong.secureservice.dto.auth.ChangePasswordRequest;
import com.sunjintong.secureservice.dto.auth.LoginRequest;
import com.sunjintong.secureservice.dto.auth.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    String refresh(String refreshToken);
}