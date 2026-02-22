package com.sunjintong.secureservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username",columnList = "username", unique = true)
})
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 47)
    private String username;
    @Column(nullable = false)
    private String passwordHash;
    @Column(length =  97)
    private String email;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
    @Column(nullable = false)
    private Integer tokenVersion;
    public  User() {
        super();
    }
}
