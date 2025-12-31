package com.mopl.domain.service.user;

import java.util.UUID;

import com.mopl.domain.exception.user.FollowNotFoundException;
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

    public void delete(FollowModel followModel) {
        followRepository.delete(followModel);
    }

    public FollowModel getById(UUID followId) {
        return followRepository.findById(followId)
            .orElseThrow(() -> new FollowNotFoundException(followId));
    }
}
