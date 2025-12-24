package com.mopl.jpa.repository.user;

import com.mopl.jpa.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByEmail(String email);
}
