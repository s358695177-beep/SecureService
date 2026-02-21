package com.sunjintong.secureservice.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sunjintong.secureservice.common.security.AuthPrincipal;
import com.sunjintong.secureservice.config.security.JwtProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtTokenVerifier {

    private final JWTVerifier verifier;

    public JwtTokenVerifier(JwtProperties properties) {
        Algorithm algorithm = Algorithm.HMAC256(properties.secret());
        this.verifier = JWT.require(algorithm)
                .withIssuer(properties.issuer())
                .build();
    }

    public AuthPrincipal verify(String token) {
        DecodedJWT jwt = verifier.verify(token);

        Long userId = Long.valueOf(jwt.getSubject());
        List<String> roles = jwt.getClaim("roles").asList(String.class);
        String jti = jwt.getId();

        return new AuthPrincipal(
                userId,
                roles == null ? List.of() : roles,
                jti
        );
    }
}