package com.sunjintong.secureservice.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sunjintong.secureservice.config.security.JwtProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenService {

    private final JwtProperties properties;
    private final Algorithm algorithm;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.algorithm = Algorithm.HMAC256(properties.secret());
    }

    public String issueAccessToken(Long userId, List<String> roles) {

        Instant now = Instant.now();
        Instant expireAt = now.plus(properties.accessTtlMinutes(), ChronoUnit.MINUTES);

        return JWT.create()
                .withIssuer(properties.issuer())
                .withSubject(String.valueOf(userId))
                .withIssuedAt(now)
                .withExpiresAt(expireAt)
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("roles", roles)
                .sign(algorithm);
    }
}