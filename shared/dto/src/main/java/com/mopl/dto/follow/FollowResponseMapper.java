package com.mopl.dto.follow;

import com.mopl.domain.model.follow.FollowModel;
import org.springframework.stereotype.Component;

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
