package com.sunjintong.secureservice.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private final String accesstoken;
    private final String refreshtoken;
    public LoginResponse(String[] tokens) {
        this.accesstoken = tokens[0];
        this.refreshtoken = tokens[1];
    }
}
