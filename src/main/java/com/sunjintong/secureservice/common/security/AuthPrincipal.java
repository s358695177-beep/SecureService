package com.sunjintong.secureservice.common.security;

import java.util.List;

public record AuthPrincipal(
        Long userId,
        List<String> roles,
        String tokenId,
        TokenType type) {
}