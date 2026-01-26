package com.mopl.domain.model.follow;

import java.util.UUID;

import com.mopl.domain.model.base.BaseModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class FollowModel extends BaseModel {

    private UUID followeeId;
    private UUID followerId;

    public static FollowModel create(
        UUID followeeId,
        UUID followerId
    ) {
        return FollowModel.builder()
            .followeeId(followeeId)
            .followerId(followerId)
            .build();
    }
}
