package com.sunjintong.secureservice.service.user;

import com.sunjintong.secureservice.dto.user.RegisterRequest;

public interface UserService {
    Long registerUser(RegisterRequest registerRequest);
}
