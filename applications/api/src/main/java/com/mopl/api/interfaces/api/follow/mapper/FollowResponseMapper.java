package com.mopl.api.interfaces.api.follow.mapper;

import com.mopl.api.interfaces.api.follow.dto.FollowResponse;
import org.springframework.stereotype.Component;

import com.mopl.domain.model.follow.FollowModel;

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
