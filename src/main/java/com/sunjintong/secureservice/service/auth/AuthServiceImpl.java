package com.sunjintong.secureservice.service.auth;

import com.sunjintong.secureservice.config.security.jwt.JwtTokenService;
import com.sunjintong.secureservice.entity.User;
import com.sunjintong.secureservice.repository.UserRepository;
import com.sunjintong.secureservice.dto.auth.LoginRequest;
import com.sunjintong.secureservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    @Override
    public String login(LoginRequest request) {
        // 1)查用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // 2)校验密码（BCrypt.matches）
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("BAD_CREDENTIALS");
        }

        List<String> roles = userRoleRepository.findByUser(user).stream()
                .map(ur -> ur.getRole().getCode())
                .distinct()
                .toList();

        // 3)签发token（roles先给最小集，后面RBAC再扩）
        return jwtTokenService.issueAccessToken(user, roles);
    }
}