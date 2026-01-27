package com.mopl.jpa.repository.user;

import com.mopl.jpa.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

    // orphan storage cleanup batch 전용
    @Query(
        value = "select 1 from users where profile_image_path = :path limit 1",
        nativeQuery = true
    )
    Integer findOneByProfileImagePath(String path);
}
