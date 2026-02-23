package com.sunjintong.secureservice.controller.auth;

import com.sunjintong.secureservice.common.security.AuthPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/me")
    public AuthPrincipal me(@AuthenticationPrincipal AuthPrincipal authentication) {
        return authentication;
    }
}