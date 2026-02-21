package com.sunjintong.secureservice.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString(exclude = "password")
public class LoginRequest {
    @NotBlank(message = "username can not be blank")
    private final String username;
    @NotBlank(message = "password can not be blank")
    private final String password;
}
