package com.sunjintong.secureservice.controller.user;

import com.sunjintong.secureservice.common.Result;
import com.sunjintong.secureservice.dto.user.RegisterRequest;
import com.sunjintong.secureservice.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register")
    public ResponseEntity<Result<Long>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        Long userId = userService.registerUser(registerRequest);
        URI location = URI.create("/users/"+userId);
        return ResponseEntity.created(location).body(Result.ok(userId));
    }
}
