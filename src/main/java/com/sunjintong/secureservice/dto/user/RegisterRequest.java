package com.sunjintong.secureservice.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString(exclude = "password")
public class RegisterRequest {
    @NotBlank(message = "username cannot be blank")
    private final String username;
    @NotBlank(message = "password cannot be blank")
    @Size(min = 9, max = 17,message = "password must contains 9-17 characters")
    private final String password;
    @Email(message = "invalid email format")
    private final String email;
}
