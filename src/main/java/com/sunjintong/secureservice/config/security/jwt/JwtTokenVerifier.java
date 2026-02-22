package com.sunjintong.secureservice.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sunjintong.secureservice.common.security.AuthPrincipal;
import com.sunjintong.secureservice.common.security.SecurityResponseWriter;
import com.sunjintong.secureservice.config.security.JwtProperties;
import com.sunjintong.secureservice.entity.User;
import com.sunjintong.secureservice.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Component
public class JwtTokenVerifier {

    private final JWTVerifier verifier;
    private final UserRepository userRepository;

    public JwtTokenVerifier(JwtProperties properties, UserRepository userRepository) {
        this.userRepository = userRepository;
        Algorithm algorithm = Algorithm.HMAC256(properties.secret());
        this.verifier = JWT.require(algorithm)
                .withIssuer(properties.issuer())
                .build();
    }

    public AuthPrincipal verify(String token) {
        DecodedJWT jwt = verifier.verify(token);
        Integer tokenVersion = jwt.getClaim("tokenVersion").asInt();
        if (tokenVersion == null) {
            throw new JWTVerificationException("Missing tokenVersion");
        }
        Long userId = Long.valueOf(jwt.getSubject());
        Optional<User> u = userRepository.findById(userId);

        if (u.isEmpty() || !Objects.equals(tokenVersion, u.get().getTokenVersion())) {
            throw  new JWTVerificationException("Invalid token version");
        }
        List<String> roles = jwt.getClaim("roles").asList(String.class);
        String jti = jwt.getId();

        return new AuthPrincipal(
                userId,
                roles == null ? List.of() : roles,
                jti
        );
    }
}