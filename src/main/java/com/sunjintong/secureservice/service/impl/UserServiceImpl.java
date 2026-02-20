package com.sunjintong.secureservice.service.impl;

import com.sunjintong.secureservice.common.BizException;
import com.sunjintong.secureservice.common.ErrorCode;
import com.sunjintong.secureservice.dto.RegisterRequest;
import com.sunjintong.secureservice.entity.User;
import com.sunjintong.secureservice.repository.UserRepository;
import com.sunjintong.secureservice.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        try{
            return userRepository.save(user).getId();
        }catch (DataIntegrityViolationException e){
            throw new BizException(ErrorCode.USERNAME_EXISTS,e);
        }
    }
}
