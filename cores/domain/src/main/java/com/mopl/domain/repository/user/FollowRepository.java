package com.mopl.domain.repository.user;

import java.util.Optional;
import java.util.UUID;

import com.mopl.domain.model.user.FollowModel;

public interface FollowRepository {

    FollowModel save(FollowModel followModel);

    /**
     * follower ID와 followee ID로 팔로우 관계 조회
     */
    Optional<FollowModel> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    /**
     * 팔로우 관계가 존재하는지 확인 (soft delete 제외)
     */
    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
}
