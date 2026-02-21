package com.sunjintong.secureservice.service.auth;

import com.sunjintong.secureservice.dto.auth.LoginRequest;

public interface AuthService {
    String login(LoginRequest request);
}