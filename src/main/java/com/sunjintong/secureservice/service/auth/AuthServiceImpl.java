package com.sunjintong.secureservice.service.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.sunjintong.secureservice.common.BizException;
import com.sunjintong.secureservice.common.ErrorCode;
import com.sunjintong.secureservice.common.security.AuthPrincipal;
import com.sunjintong.secureservice.common.security.TokenType;
import com.sunjintong.secureservice.config.security.jwt.JwtTokenService;
import com.sunjintong.secureservice.config.security.jwt.JwtTokenVerifier;
import com.sunjintong.secureservice.dto.auth.ChangePasswordRequest;
import com.sunjintong.secureservice.entity.RefreshToken;
import com.sunjintong.secureservice.entity.User;
import com.sunjintong.secureservice.repository.RefreshTokenRepository;
import com.sunjintong.secureservice.repository.UserRepository;
import com.sunjintong.secureservice.dto.auth.LoginRequest;
import com.sunjintong.secureservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenVerifier jwtTokenVerifier;

    @Override
    public String[] login(LoginRequest request) {
        // 1)查用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND));

        // 2)校验密码（BCrypt.matches）
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.BAD_CREDENTIALS);
        }

        List<String> roles = userRoleRepository.findByUser(user).stream()
                .map(ur -> ur.getRole().getCode())
                .distinct()
                .toList();

        // 3)签发token（roles先给最小集，后面RBAC再扩）
        return new String[]{jwtTokenService.issueAccessToken(user, roles, TokenType.ACCESS), jwtTokenService.issueAccessToken(user, roles, TokenType.REFRESH)};
    }

    @Transactional
    public void changePassword(Long userId,ChangePasswordRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.BAD_CREDENTIALS);
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        int dbvt = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
        user.setTokenVersion(dbvt+1);
        userRepository.save(user);
    }


    @Transactional
    public String refresh(String refreshToken) {
        Instant now = Instant.now();
        try {
            AuthPrincipal principal = jwtTokenVerifier.verify(refreshToken);
            if (principal.type().equals(TokenType.ACCESS)) {
                throw new BizException(ErrorCode.UNAUTHORIZED);
            }
            RefreshToken token = refreshTokenRepository.findByTokenId(principal.tokenId())
                    .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED));
            if (token.isRevoked() || token.getExpiresAt().isBefore(now)) {
                throw new BizException(ErrorCode.UNAUTHORIZED);
            }
            User user = userRepository.findById(principal.userId()).orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND));
            return jwtTokenService.issueAccessToken(user,principal.roles(),TokenType.ACCESS);
        }catch (JWTVerificationException | IllegalArgumentException e) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
    }
}