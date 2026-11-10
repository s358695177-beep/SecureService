package com.sunjintong.secureservice.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunjintong.secureservice.common.security.SecurityResponseWriter;
import com.sunjintong.secureservice.config.security.jwt.JwtAuthenticationFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@EnableMethodSecurity
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        CorsConfiguration loginCors = new CorsConfiguration();
        loginCors.setAllowedOrigins(List.of("http://127.0.0.1:8000"));
        loginCors.setAllowedMethods(List.of("POST"));
        loginCors.setAllowedHeaders(List.of("Content-Type"));

        UrlBasedCorsConfigurationSource corsSource =
                new UrlBasedCorsConfigurationSource();
        CorsConfiguration authCookieCors = new CorsConfiguration();
        authCookieCors.setAllowedOrigins(List.of("http://127.0.0.1:8000"));
        authCookieCors.setAllowedMethods(List.of("POST"));
        authCookieCors.setAllowedHeaders(List.of("Content-Type"));
        authCookieCors.setAllowCredentials(true);
        corsSource.registerCorsConfiguration("/auth/login", authCookieCors);
        corsSource.registerCorsConfiguration("/auth/refresh", authCookieCors);
        corsSource.registerCorsConfiguration("/auth/refresh/logout", authCookieCors);
        corsSource.registerCorsConfiguration("/users/register", loginCors);

        CorsConfiguration profileCors = new CorsConfiguration();
        profileCors.setAllowedOrigins(List.of("http://127.0.0.1:8000"));
        profileCors.setAllowedMethods(List.of("GET"));
        profileCors.setAllowedHeaders(List.of("Authorization"));
        corsSource.registerCorsConfiguration("/users/profile", profileCors);

        CorsConfiguration ticketCors = new CorsConfiguration();
        ticketCors.setAllowedOrigins(List.of("http://127.0.0.1:8000"));
        ticketCors.setAllowedMethods(List.of("POST"));
        ticketCors.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        corsSource.registerCorsConfiguration("/tickets", ticketCors);

        return http
                .cors(cors -> cors.configurationSource(corsSource))
                // 1) 先关闭csrf（你当前是API服务，且用JWT）
                .csrf(AbstractHttpConfigurer::disable)

                // 2) 路径权限规则
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/refresh").permitAll()
                       .requestMatchers(org.springframework.http.HttpMethod.POST, "/auth/refresh/logout").permitAll()
                        .requestMatchers("/users/register").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                ).exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            SecurityContextHolder.clearContext();
                            SecurityResponseWriter.writeUnauthorized(res);
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            if (!res.isCommitted()) {
                                SecurityResponseWriter.writeForbidden(res);
                            }
                        })
                ).sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 3) 把你的JWT Filter挂到Spring Security链路里
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}
