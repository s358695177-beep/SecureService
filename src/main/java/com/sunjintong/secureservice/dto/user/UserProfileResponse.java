package com.sunjintong.secureservice.dto.user;

import java.time.Instant;

public record UserProfileResponse(
        Long userId,
        String username,
        Instant createdAt
) {
}