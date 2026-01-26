package com.mopl.jpa.repository.follow;

import com.mopl.jpa.entity.follow.FollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaFollowRepository extends JpaRepository<FollowEntity, UUID> {

    Optional<FollowEntity> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    @Query("SELECT f.follower.id FROM FollowEntity f WHERE f.followee.id = :followeeId")
    List<UUID> findFollowerIdsByFolloweeId(UUID followeeId);

    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    long countByFolloweeId(UUID followeeId);
}
