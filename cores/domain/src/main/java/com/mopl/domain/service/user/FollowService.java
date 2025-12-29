package com.mopl.domain.service.user;

import com.mopl.domain.exception.user.SelfFollowException;
import com.mopl.domain.model.user.FollowModel;
import com.mopl.domain.repository.user.FollowRepository;

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
