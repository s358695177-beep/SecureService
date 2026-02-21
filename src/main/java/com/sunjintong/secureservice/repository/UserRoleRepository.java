package com.sunjintong.secureservice.repository;

import com.sunjintong.secureservice.entity.User;
import com.sunjintong.secureservice.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUser(User user);
}