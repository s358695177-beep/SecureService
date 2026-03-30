package com.sunjintong.secureservice.service.auth;

import com.sunjintong.secureservice.dto.auth.ChangePasswordRequest;
import com.sunjintong.secureservice.dto.auth.LoginRequest;
import com.sunjintong.secureservice.dto.auth.LoginResponse;
import com.sunjintong.secureservice.dto.auth.RefreshResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    RefreshResponse refresh(String refreshToken);
    void revokeAllUserTokens(Long userId);
}