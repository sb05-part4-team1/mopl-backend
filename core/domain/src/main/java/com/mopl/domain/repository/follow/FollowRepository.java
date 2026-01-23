package com.mopl.domain.repository.follow;

import com.mopl.domain.model.follow.FollowModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository {

    Optional<FollowModel> findById(UUID followId);

    Optional<FollowModel> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    List<UUID> findFollowerIdsByFolloweeId(UUID followeeId);

    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    long countByFolloweeId(UUID followeeId);

    FollowModel save(FollowModel followModel);

    void delete(FollowModel followModel);
}
