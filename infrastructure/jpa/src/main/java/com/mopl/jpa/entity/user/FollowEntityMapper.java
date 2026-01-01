package com.mopl.jpa.entity.user;

import org.springframework.stereotype.Component;

import com.mopl.domain.model.user.FollowModel;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FollowEntityMapper {

    // Proxy를 사용해서 성능 최적화하기
    private final EntityManager em;

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

        UserEntity followee = em.getReference(UserEntity.class, followModel.getFolloweeId());
        UserEntity follower = em.getReference(UserEntity.class, followModel.getFollowerId());

        return FollowEntity.builder()
            .followee(followee)
            .follower(follower)
            .build();
    }
}
