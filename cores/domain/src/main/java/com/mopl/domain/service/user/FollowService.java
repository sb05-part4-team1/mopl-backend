package com.mopl.domain.service.user;

import org.springframework.stereotype.Component;

import com.mopl.domain.model.user.FollowModel;
import com.mopl.domain.repository.user.FollowRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    public FollowModel create(FollowModel followModel) {

        if (followModel.getFollowerId().equals(followModel.getFolloweeId())) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        return followRepository.findByFollowerIdAndFolloweeId(followModel.getFollowerId(),
            followModel.getFolloweeId())
            .map(follow -> {
                if (follow.isDeleted()) {
                    follow.restore(); // 삭제된 경우 복구
                    return followRepository.save(follow);
                }
                throw new IllegalStateException("이미 팔로우 중인 사용자입니다.");
            })
            .orElseGet(() -> followRepository.save(FollowModel.create(followModel.getFolloweeId(),
                followModel.getFollowerId())));
    }
}
