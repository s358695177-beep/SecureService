package com.sunjintong.secureservice.service.user;

import com.sunjintong.secureservice.common.BizException;
import com.sunjintong.secureservice.common.ErrorCode;
import com.sunjintong.secureservice.config.security.jwt.JwtTokenService;
import com.sunjintong.secureservice.dto.user.RegisterRequest;
import com.sunjintong.secureservice.entity.Role;
import com.sunjintong.secureservice.entity.User;
import com.sunjintong.secureservice.entity.UserRole;
import com.sunjintong.secureservice.repository.RoleRepository;
import com.sunjintong.secureservice.repository.UserRepository;
import com.sunjintong.secureservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenService jwtTokenService;

    @Transactional
    @Override
    public Long registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BizException(ErrorCode.USERNAME_EXISTS);
        }
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setTokenVersion(0);
        try{
            User savedUser = userRepository.save(user);
            Role userRole = roleRepository.findByCode("USER")
                    .orElseGet(() -> roleRepository.save(new Role("USER", "普通用户")));
            userRoleRepository.save(new UserRole(savedUser, userRole));
            return savedUser.getId();
        }catch (DataIntegrityViolationException e){
            throw new BizException(ErrorCode.USERNAME_EXISTS,e);
        }
    }
}
