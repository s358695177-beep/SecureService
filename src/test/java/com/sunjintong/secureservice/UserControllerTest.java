package com.sunjintong.secureservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunjintong.secureservice.dto.RegisterRequest;
import com.sunjintong.secureservice.entity.User;
import com.sunjintong.secureservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    private static final String REGISTER_URL = "/users/register";

    // 统一用常量，避免测试里散落魔法数字
    private static final int CODE_SUCCESS = 0;
    private static final int CODE_PARAM_INVALID = 1001;
    private static final int CODE_USERNAME_EXISTS = 1002;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder; // 用生产同款Bean，避免测试与生产不一致

    @Test
    void register_success_shouldReturn201_andPersistBCryptHash() throws Exception {
        // Given
        String rawPassword = "P@ssw0rd123";
        RegisterRequest req = buildRegisterRequest("alice", rawPassword, "alice@example.com");

        // When/Then
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(CODE_SUCCESS))
                .andExpect(jsonPath("$.data").isNumber());

        // And:验证落库密码不是明文，且确实可被原始密码matches
        User saved = userRepository.findByUsername("alice").orElseThrow();
        assertThat(saved.getPasswordHash()).isNotBlank();
        assertThat(saved.getPasswordHash()).isNotEqualTo(rawPassword);
        assertThat(saved.getPasswordHash()).startsWith("$2"); // BCrypt特征
        assertThat(passwordEncoder.matches(rawPassword, saved.getPasswordHash())).isTrue(); // 能抓“双重加密/错误加密”
    }

    @Test
    void register_duplicateUsername_shouldReturn409_andBusinessCode1002() throws Exception {
        // Given:先插入一个已存在用户
        persistUser("bob", "whatever123", "bob@example.com");

        // When:再注册同名用户（注意：password必须满足你的DTO校验规则）
        RegisterRequest req = buildRegisterRequest("bob", "newpass123", "bob2@example.com");

        // Then
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                // .andDo(print()) // 需要排错时打开
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(CODE_USERNAME_EXISTS));
    }

    @Test
    void register_invalidParam_shouldReturn400_andBusinessCode1001() throws Exception {
        // Given:构造一个明显不合法的请求（触发@NotBlank/@Email/@Size等）
        RegisterRequest req = buildRegisterRequest("", "123", "bad-email");

        // Then
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                // .andDo(print()) // 需要排错时打开
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(CODE_PARAM_INVALID));
    }

    // ---------- helpers ----------

    private RegisterRequest buildRegisterRequest(String username, String password, String email) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setPassword(password);
        req.setEmail(email);
        return req;
    }

    private void persistUser(String username, String rawPassword, String email) {
        LocalDateTime now = LocalDateTime.now();
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setEmail(email);
        u.setCreatedAt(now);
        u.setUpdatedAt(now);
        userRepository.save(u);
    }
}