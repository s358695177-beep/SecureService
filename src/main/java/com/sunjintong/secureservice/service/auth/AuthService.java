package com.sunjintong.secureservice.service.auth;

import com.sunjintong.secureservice.dto.auth.ChangePasswordRequest;
import com.sunjintong.secureservice.dto.auth.LoginRequest;

public interface AuthService {
    String[] login(LoginRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    String refresh(String refreshToken);
}