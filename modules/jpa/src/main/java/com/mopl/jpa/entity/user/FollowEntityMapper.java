package com.mopl.jpa.entity.user;

import org.springframework.stereotype.Component;

import com.mopl.domain.model.user.FollowModel;
import com.mopl.jpa.repository.user.JpaUserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FollowEntityMapper {

    private final JpaUserRepository jpaUserRepository;

    public FollowModel toModel(FollowEntity followEntity) {
        if (followEntity == null) {
            return null;
        }

        return FollowModel.builder()
            .id(followEntity.getId())
            .followeeId(followEntity.getFollowee().getId())
            .followerId(followEntity.getFollower().getId())
            .createdAt(followEntity.getCreatedAt())
            .deletedAt(followEntity.getDeletedAt())
            .build();
    }

    public FollowEntity toEntity(FollowModel followModel) {

        UserEntity followee = jpaUserRepository.findById(followModel.getFolloweeId())
            .orElseThrow(() -> new IllegalArgumentException("팔로우할 사용자가 없습니다."));

        UserEntity follower = jpaUserRepository.findById(followModel.getFollowerId())
            .orElseThrow(() -> new IllegalArgumentException("팔로워가 없습니다."));

        return FollowEntity.builder()
            .followee(followee)
            .follower(follower)
            .build();
    }
}
