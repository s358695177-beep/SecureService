package com.sunjintong.secureservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "refresh_tokens",
        indexes = {@Index(name = "token",columnList = "token_Id"),
                @Index(name = "user",columnList = "user_Id")})
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, name = "user_Id")
    private long userId;
    @Column(nullable = false, name = "token_Id",length = 64)
    private String token;
    @Column(nullable = false, name = "created_At")
    private Instant createdAt;
    @Column(nullable = false, name = "expires_At")
    private Instant expiresAt;
    @Column(nullable = false, name = "revoked")
    private boolean revoked;
}
