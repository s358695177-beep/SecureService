package com.sunjintong.secureservice.repository;

import com.sunjintong.secureservice.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    @Modifying
    @Transactional
    @Query("update User u set u.tokenVersion = coalesce(u.tokenVersion, 0) + 1 where u.id = :id")
    int bumpTokenVersion(@Param("id") Long id);
}
