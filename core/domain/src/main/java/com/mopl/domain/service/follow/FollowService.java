package com.mopl.domain.service.follow;

import com.mopl.domain.exception.follow.FollowNotFoundException;
import com.mopl.domain.exception.follow.SelfFollowException;
import com.mopl.domain.model.follow.FollowModel;
import com.mopl.domain.repository.follow.FollowRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    public FollowModel getById(UUID followId) {
        return followRepository.findById(followId)
            .orElseThrow(() -> FollowNotFoundException.withId(followId));
    }

    public List<UUID> getFollowerIds(UUID followeeId) {
        return followRepository.findFollowerIdsByFolloweeId(followeeId);
    }

    public long getFollowerCount(UUID followeeId) {
        return followRepository.countByFolloweeId(followeeId);
    }

    public boolean isFollow(UUID followerId, UUID followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    public FollowModel create(FollowModel followModel) {
        if (followModel.getFollowerId().equals(followModel.getFolloweeId())) {
            throw SelfFollowException.withUserId(followModel.getFollowerId());
        }

        return followRepository.findByFollowerIdAndFolloweeId(
                followModel.getFollowerId(), followModel.getFolloweeId())
            .orElseGet(() -> followRepository.save(followModel));
    }

    public void delete(FollowModel followModel) {
        followRepository.delete(followModel);
    }
}
