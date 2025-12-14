package com.jk.socialsync.repositories;

import com.jk.socialsync.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    Optional<UserEntity> findByUsername(String username);
}
