package com.mopl.api.application.user;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mopl.domain.model.user.FollowModel;
import com.mopl.domain.service.user.FollowService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FollowFacade {

    private final FollowService followService;

    public FollowModel follow(UUID followerId, UUID followeeId) {
        FollowModel followModel = FollowModel.create(followeeId, followerId);
        return followService.create(followModel);
    }

}
