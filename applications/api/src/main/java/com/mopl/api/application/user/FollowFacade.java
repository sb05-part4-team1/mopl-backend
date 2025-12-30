package com.mopl.api.application.user;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mopl.domain.model.user.FollowModel;
import com.mopl.domain.service.user.FollowService;
import com.mopl.domain.service.user.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FollowFacade {

    private final FollowService followService;
    private final UserService userService;

    public FollowModel follow(UUID followerId, UUID followeeId) {
        userService.getById(followerId);
        userService.getById(followeeId);

        FollowModel followModel = FollowModel.create(followeeId, followerId);
        return followService.create(followModel);
    }

}
