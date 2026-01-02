package com.mopl.api.application.user;

import com.mopl.domain.exception.user.AccessDeniedException;
import com.mopl.domain.model.user.FollowModel;
import com.mopl.domain.service.user.FollowService;
import com.mopl.domain.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FollowFacade {

    private final FollowService followService;
    private final UserService userService;

    @Transactional
    public FollowModel follow(UUID followerId, UUID followeeId) {
        userService.getById(followerId);
        userService.getById(followeeId);

        FollowModel followModel = FollowModel.create(followeeId, followerId);
        return followService.create(followModel);
    }

    @Transactional
    public void unFollow(UUID userId, UUID followId) {
        userService.getById(userId);

        FollowModel follow = followService.getById(followId);

        if (!follow.getFollowerId().equals(userId)) {
            throw new AccessDeniedException(userId, followId);
        }

        followService.delete(follow);
    }

}
