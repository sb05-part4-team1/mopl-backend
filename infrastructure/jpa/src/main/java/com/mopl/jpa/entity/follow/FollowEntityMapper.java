package com.mopl.jpa.entity.follow;

import com.mopl.domain.model.follow.FollowModel;
import com.mopl.jpa.entity.user.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FollowEntityMapper {

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
        if (followModel == null) {
            return null;
        }

        return FollowEntity.builder()
            .id(followModel.getId())
            .createdAt(followModel.getCreatedAt())
            .deletedAt(followModel.getDeletedAt())
            .followee(toUserEntity(followModel.getFolloweeId()))
            .follower(toUserEntity(followModel.getFollowerId()))
            .build();
    }

    private UserEntity toUserEntity(UUID userId) {
        return userId != null
            ? UserEntity.builder().id(userId).build()
            : null;
    }
}
