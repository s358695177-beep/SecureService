package com.sunjintong.secureservice.controller.auth;

import com.sunjintong.secureservice.common.ErrorCode;
import com.sunjintong.secureservice.common.Result;
import com.sunjintong.secureservice.common.security.AuthPrincipal;
import com.sunjintong.secureservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profile")
    public ResponseEntity<?> AdminProfile( @AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok().body(Result.ok(principal));
    }
}