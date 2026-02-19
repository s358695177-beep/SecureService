package com.sunjintong.secureservice.service;

import com.sunjintong.secureservice.dto.RegisterRequest;

public interface UserService {
    Long registerUser(RegisterRequest registerRequest);
}
