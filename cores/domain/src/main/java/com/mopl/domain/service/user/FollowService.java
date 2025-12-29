package com.mopl.domain.service.user;

import com.mopl.domain.model.user.FollowModel;
import com.mopl.domain.repository.user.FollowRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    public FollowModel create(FollowModel followModel) {

        if (followModel.getFollowerId().equals(followModel.getFolloweeId())) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        return followRepository.findByFollowerIdAndFolloweeId(followModel.getFollowerId(),
            followModel.getFolloweeId())
            .orElseGet(() -> followRepository.save(FollowModel.create(followModel.getFolloweeId(),
                followModel.getFollowerId())));
    }
}
