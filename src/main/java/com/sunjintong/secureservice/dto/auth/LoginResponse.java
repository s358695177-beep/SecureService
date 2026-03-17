package com.sunjintong.secureservice.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private final String accessToken;
    private final String refreshToken;
    public LoginResponse(String[] tokens) {
        this.accessToken = tokens[0];
        this.refreshToken = tokens[1];
    }
}
