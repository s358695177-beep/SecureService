package com.sunjintong.secureservice.controller.admin;

import com.sunjintong.secureservice.common.ErrorCode;
import com.sunjintong.secureservice.common.Result;
import com.sunjintong.secureservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    @PostMapping("/users/{id}/invalidate")
    public ResponseEntity<Result<String>> invalidate(@PathVariable Long id) {
        int updated = userRepository.bumpTokenVersion(id);
        if (updated == 1) {
            return ResponseEntity.ok(Result.ok("done"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.fail(ErrorCode.PARAM_INVALID));
    }
}