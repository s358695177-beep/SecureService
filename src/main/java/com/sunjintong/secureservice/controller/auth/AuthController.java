package com.sunjintong.secureservice.controller.auth;

import com.sunjintong.secureservice.common.BizException;
import com.sunjintong.secureservice.common.ErrorCode;
import com.sunjintong.secureservice.common.Result;
import com.sunjintong.secureservice.common.security.AuthPrincipal;
import com.sunjintong.secureservice.dto.auth.*;
import com.sunjintong.secureservice.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Result> login(@RequestBody LoginRequest request){

        LoginResponse loginResponse = authService.login(request);

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false) // localhost开发先false
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Result.ok(loginResponse.getAccessToken()));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword( @AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody ChangePasswordRequest request){
        authService.changePassword(principal.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Result> refresh(@CookieValue(name = "refreshToken" , required = false) String refreshToken){
        if (refreshToken == null){
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }

        RefreshResponse refreshResponse = authService.refresh(refreshToken);

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", refreshResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false) // localhost开发先false
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Result.ok(refreshResponse.getAccessToken()));
    }
}
