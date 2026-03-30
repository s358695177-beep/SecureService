package com.sunjintong.secureservice.service.auth;

import com.sunjintong.secureservice.common.BizException;
import com.sunjintong.secureservice.common.ErrorCode;
import com.sunjintong.secureservice.config.security.jwt.JwtTokenService;
import com.sunjintong.secureservice.dto.auth.ChangePasswordRequest;
import com.sunjintong.secureservice.dto.auth.LoginResponse;
import com.sunjintong.secureservice.dto.auth.RefreshResponse;
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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
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
        revokeAllUserTokens(user.getId());
        String accessToken = jwtTokenService.issueAccessToken(user, roles);
        String refreshToken = "rt_" + UUID.randomUUID().toString().replace("-", "");
        Instant now = Instant.now();
        RefreshToken token = new RefreshToken();
        token.setToken(refreshToken);
        token.setRevoked(false);
        token.setExpiresAt(now.plus(Duration.ofDays(7)));
        token.setUserId(user.getId());
        token.setCreatedAt(now);
        //refreshTokenRepository.revokeAllByUserId(user.getId());
        refreshTokenRepository.save(token);
        // 3)签发token（roles先给最小集，后面RBAC再扩）
        return new LoginResponse(accessToken, refreshToken);
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
    public RefreshResponse refresh(String refreshToken) {
        Instant now = Instant.now();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }

        if (!refreshToken.startsWith("rt_")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        String raw = refreshToken.substring(3);
        if (!raw.matches("^[0-9a-fA-F]{32}$")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED));
        if (token.isRevoked() || token.getExpiresAt().isBefore(now)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED));
        List<String> roles = userRoleRepository.findByUser(user).stream()
                .map(ur -> ur.getRole().getCode())
                .distinct()
                .toList();
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        String accessToken = jwtTokenService.issueAccessToken(user, roles);
        refreshToken = "rt_" + UUID.randomUUID().toString().replace("-", "");
        RefreshToken newToken = new RefreshToken();
        newToken.setToken(refreshToken);
        newToken.setRevoked(false);
        newToken.setExpiresAt(now.plus(Duration.ofDays(7)));
        newToken.setUserId(user.getId());
        newToken.setCreatedAt(now);
        refreshTokenRepository.save(newToken);
        return new RefreshResponse(accessToken,refreshToken);
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        int count = refreshTokenRepository.revokeAllByUserId(userId);
    }

}