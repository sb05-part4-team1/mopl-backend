package com.mopl.jpa.repository.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.jpa.entity.user.FollowEntity;

public interface JpaFollowRepository extends JpaRepository<FollowEntity, UUID> {

    Optional<FollowEntity> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
}
