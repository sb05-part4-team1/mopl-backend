package com.mopl.api.application.follow;

import com.mopl.domain.exception.follow.FollowNotAllowedException;
import com.mopl.domain.model.follow.FollowModel;
import com.mopl.domain.model.notification.NotificationLevel;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.user.UserService;
import com.mopl.sse.application.SseFacade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FollowFacade {

    private final FollowService followService;
    private final UserService userService;
    private final SseFacade sseFacade;

    @Transactional
    public FollowModel follow(UUID followerId, UUID followeeId) {
        UserModel follower = userService.getById(followerId);
        userService.getById(followeeId);

        FollowModel followModel = FollowModel.create(followeeId, followerId);
        FollowModel savedFollow = followService.create(followModel);

        NotificationModel notification = NotificationModel.create(
            "새로운 팔로우",
            follower.getName() + "님이 팔로우했습니다.",
            NotificationLevel.INFO,
            followeeId
        );

        sseFacade.sendNotification(notification);

        return savedFollow;
    }

    @Transactional
    public void unFollow(UUID userId, UUID followId) {
        userService.getById(userId);

        FollowModel follow = followService.getById(followId);

        if (!follow.getFollowerId().equals(userId)) {
            throw new FollowNotAllowedException(userId, followId);
        }

        followService.delete(follow);
    }

    @Transactional
    public long getFollowerCount(UUID followeeId) {
        userService.getById(followeeId);
        return followService.getFollowerCount(followeeId);
    }

    @Transactional(readOnly = true)
    public boolean isFollow(UUID followerId, UUID followeeId) {
        userService.getById(followerId);
        userService.getById(followeeId);
        return followService.isFollow(followerId, followeeId);
    }
}
