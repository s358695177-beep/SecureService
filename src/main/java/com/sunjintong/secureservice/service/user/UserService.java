package com.sunjintong.secureservice.service.user;

import com.sunjintong.secureservice.dto.user.RegisterRequest;
import com.sunjintong.secureservice.dto.user.UserProfileResponse;

public interface UserService {

    Long registerUser(RegisterRequest registerRequest);

    UserProfileResponse getCurrentUserProfile(Long userId);
}