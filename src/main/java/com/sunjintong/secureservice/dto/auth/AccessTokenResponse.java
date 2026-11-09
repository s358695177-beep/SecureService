package com.sunjintong.secureservice.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class AccessTokenResponse {
    String accessToken;
}
