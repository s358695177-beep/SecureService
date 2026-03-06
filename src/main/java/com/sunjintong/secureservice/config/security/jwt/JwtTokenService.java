package com.sunjintong.secureservice.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sunjintong.secureservice.common.BizException;
import com.sunjintong.secureservice.common.ErrorCode;
import com.sunjintong.secureservice.common.security.TokenType;
import com.sunjintong.secureservice.config.security.JwtProperties;
import com.sunjintong.secureservice.entity.User;
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

    public String issueAccessToken(User user, List<String> roles, TokenType  tokenType) {
        Instant now = Instant.now();
        return switch (tokenType.getType()) {
            case "access" -> JWT.create()
                    .withIssuer(properties.issuer())
                    .withSubject(String.valueOf(user.getId()))
                    .withIssuedAt(now)
                    .withExpiresAt(now.plus(15, ChronoUnit.MINUTES))
                    .withJWTId(UUID.randomUUID().toString())
                    .withClaim("roles", roles)
                    .withClaim("tokenVersion", user.getTokenVersion())
                    .withClaim("tokenType", tokenType.getType())
                    .sign(algorithm);
            case "refresh" -> JWT.create()
                    .withIssuer(properties.issuer())
                    .withSubject(String.valueOf(user.getId()))
                    .withIssuedAt(now)
                    .withExpiresAt(now.plus(7, ChronoUnit.DAYS))
                    .withJWTId(UUID.randomUUID().toString())
                    .withClaim("roles", roles)
                    .withClaim("tokenVersion", user.getTokenVersion())
                    .withClaim("tokenType", tokenType.getType())
                    .sign(algorithm);
            default -> throw new BizException(ErrorCode.BAD_CREDENTIALS);
        };
    }
}