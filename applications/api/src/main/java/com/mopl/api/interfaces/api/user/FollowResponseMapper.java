package com.mopl.api.interfaces.api.user;

import org.springframework.stereotype.Component;

import com.mopl.domain.model.user.FollowModel;

@Component
public class FollowResponseMapper {

    public FollowResponse toResponse(FollowModel followModel) {
        return new FollowResponse(
            followModel.getId(),
            followModel.getFolloweeId(),
            followModel.getFollowerId()
        );
    }
}
