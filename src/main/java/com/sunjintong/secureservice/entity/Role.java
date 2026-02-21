package com.sunjintong.secureservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_roles_code", columnNames = "code")
})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 建议存USER/ADMIN这种，不存ROLE_USER；ROLE_前缀交给Spring层统一补
    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 64)
    private String name;

    public Role(String code, String name) {
        this.code = code;
        this.name = name;
    }
}