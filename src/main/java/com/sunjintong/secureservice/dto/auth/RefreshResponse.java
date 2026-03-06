package com.sunjintong.secureservice.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class RefreshResponse {
    private final String access_token;
}
