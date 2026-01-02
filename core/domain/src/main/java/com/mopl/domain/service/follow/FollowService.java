package com.mopl.domain.service.follow;

import com.mopl.domain.exception.follow.SelfFollowException;
import com.mopl.domain.model.follow.FollowModel;
import com.mopl.domain.repository.follow.FollowRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    public FollowModel create(FollowModel followModel) {

        if (followModel.getFollowerId().equals(followModel.getFolloweeId())) {
            throw new SelfFollowException(followModel.getFollowerId());
        }

        return followRepository.findByFollowerIdAndFolloweeId(followModel.getFollowerId(),
            followModel.getFolloweeId())
            .orElseGet(() -> followRepository.save(followModel));
    }
}
