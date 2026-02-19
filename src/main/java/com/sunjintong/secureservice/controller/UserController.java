package com.sunjintong.secureservice.controller;

import com.sunjintong.secureservice.common.Result;
import com.sunjintong.secureservice.dto.RegisterRequest;
import com.sunjintong.secureservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("register")
    public Result<Long> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        Long userId = userService.registerUser(registerRequest);
        return Result.ok(userId);
    }
}
