package com.sunjintong.secureservice;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunjintong.secureservice.dto.auth.ChangePasswordRequest;
import com.sunjintong.secureservice.dto.auth.LoginRequest;
import com.sunjintong.secureservice.dto.auth.LoginResponse;
import com.sunjintong.secureservice.dto.user.RegisterRequest;
import com.sunjintong.secureservice.entity.Role;
import com.sunjintong.secureservice.entity.User;
import com.sunjintong.secureservice.entity.UserRole;
import com.sunjintong.secureservice.repository.RoleRepository;
import com.sunjintong.secureservice.repository.UserRepository;
import com.sunjintong.secureservice.repository.UserRoleRepository;
import com.sunjintong.secureservice.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRoleRepository userRoleRepository;
    String LOGIN_URL = "/auth/login";
    String Register_URL = "/users/register";
    String CHANGE_PASSWORD_URL = "/auth/password";
    String PROFILE_URL = "/users/profile";
    String ADMIN_PROFILE_URL = "/admin/profile";
    String REFRESH_URL = "/auth/refresh";
    private static final int CODE_USERNAME_EXISTS = 1002;
    @Test
    void shouldInvalidateOldTokenAfterPasswordChange() throws Exception {
        // Given:先插入一个已存在用户
        //persistUser("bob", "whatever123", "bob@example.com");
        mockMvc.perform(post(Register_URL).contentType(MediaType.APPLICATION_JSON)
                                       .content(objectMapper.writeValueAsString(new RegisterRequest("bob", "whatever123", "bob@example.com"))))
                                       .andExpect(status().isCreated());

        // Then
        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .content(objectMapper.writeValueAsString(new LoginRequest("bob", "whatever123"))))
                                       .andExpect(status().isOk())
                                       .andReturn();
        String resultString = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(resultString, LoginResponse.class);
        String token = loginResponse.getAccessToken();
        mockMvc.perform(get(PROFILE_URL).contentType(MediaType.APPLICATION_JSON)
                                        .header("Authorization", "Bearer " + token))
                                        .andDo(print())
                                        .andExpect(status().isOk());

        mockMvc.perform(put(CHANGE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("whatever123", "123whatever"))))
                        .andDo(print())
                        .andExpect(status().isNoContent());

        mockMvc.perform(get(PROFILE_URL).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        loginResult = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("bob", "123whatever"))))
                        .andExpect(status().isOk())
                        .andReturn();
        resultString =  loginResult.getResponse().getContentAsString();
        loginResponse =  objectMapper.readValue(resultString, LoginResponse.class);
        token = loginResponse.getAccessToken();

        mockMvc.perform(get(PROFILE_URL).contentType(MediaType.APPLICATION_JSON)
                                        .header("Authorization", "Bearer " + token))
                                        .andExpect(status().isOk());

    }

    @Test
    void auth() throws Exception {
        mockMvc.perform(get(PROFILE_URL).contentType(MediaType.APPLICATION_JSON))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized());

        mockMvc.perform(post(Register_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("bob", "whatever123", "bob@example.com"))))
                .andExpect(status().isCreated());

        // Then
        MvcResult loginResult1 = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("bob123", "whatever123"))))
                .andExpect(status().isNotFound())
                .andReturn();

        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("bob", "whatever123"))))
                .andExpect(status().isOk())
                .andReturn();
        String resultString = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(resultString, LoginResponse.class);
        String token = loginResponse.getAccessToken();

        mockMvc.perform(put(CHANGE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("whatever123123123", "123whatever"))))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put(CHANGE_PASSWORD_URL).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("whatever123", "123whatever"))))
                .andDo(print())
                .andExpect(status().isNoContent());
        mockMvc.perform(get(PROFILE_URL).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        MvcResult loginResult2 = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("bob", "whatever123"))))
                .andExpect(status().isUnauthorized())
                .andReturn();

    }

    @Test
    void admin() throws Exception {
        mockMvc.perform(get(ADMIN_PROFILE_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(Register_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("bob", "whatever123", "bob@example.com"))))
                .andExpect(status().isCreated());
        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("bob", "whatever123"))))
                .andExpect(status().isOk())
                .andReturn();
        String resultString = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(resultString, LoginResponse.class);
        String token = loginResponse.getAccessToken();

        mockMvc.perform(get(ADMIN_PROFILE_URL).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)).andDo(print())
                        .andExpect(status().isForbidden());

        Role role = new Role("ADMIN","管理员");
        roleRepository.save(role);
        UserRole userRole = new UserRole(userRepository.findByUsername("bob").get(),role);
        userRoleRepository.save(userRole);
        loginResult = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("bob", "whatever123"))))
                .andExpect(status().isOk())
                .andReturn();
        resultString = loginResult.getResponse().getContentAsString();
        loginResponse = objectMapper.readValue(resultString, LoginResponse.class);
        token = loginResponse.getAccessToken();

        mockMvc.perform(get(ADMIN_PROFILE_URL).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)).andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    void login() throws Exception {
        mockMvc.perform(post(Register_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("bob", "whatever123", "bob@example.com"))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("bob", "whatever123"))))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn();

        String setCookieHeader = loginResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);

        String refreshToken = setCookieHeader.split(";", 2)[0].split("=", 2)[1];

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

        mockMvc.perform(post(REFRESH_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(refreshCookie))
                .andExpect(status().isOk());
    }


}
