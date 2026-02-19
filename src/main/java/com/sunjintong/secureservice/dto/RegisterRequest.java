package com.sunjintong.secureservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = "password")
public class RegisterRequest {
    @NotBlank(message = "username cannot be blank")
    private String username;
    @NotBlank(message = "password cannot be blank")
    @Size(min = 9, max = 17,message = "password must contains 9-17 characters")
    private String password;
    @Email(message = "invalid email format")
    private String email;
}
