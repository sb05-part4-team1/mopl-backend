package com.mopl.api.application.follow;

import com.mopl.api.application.outbox.DomainEventOutboxMapper;
import com.mopl.domain.event.user.UserFollowedEvent;
import com.mopl.domain.event.user.UserUnfollowedEvent;
import com.mopl.domain.exception.follow.FollowNotAllowedException;
import com.mopl.domain.model.follow.FollowModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.outbox.OutboxService;
import com.mopl.domain.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FollowFacade {

    private final FollowService followService;
    private final UserService userService;
    private final OutboxService outboxService;
    private final DomainEventOutboxMapper domainEventOutboxMapper;
    private final TransactionTemplate transactionTemplate;

    public FollowModel follow(UUID followerId, UUID followeeId) {
        UserModel follower = userService.getById(followerId);
        UserModel followee = userService.getById(followeeId);

        FollowModel followModel = FollowModel.create(followeeId, followerId);
        UserFollowedEvent event = UserFollowedEvent.builder()
            .followerId(follower.getId())
            .followerName(follower.getName())
            .followeeId(followee.getId())
            .build();

        return transactionTemplate.execute(status -> {
            FollowModel savedFollow = followService.create(followModel);
            outboxService.save(domainEventOutboxMapper.toOutboxModel(event));
            return savedFollow;
        });
    }

    public void unFollow(UUID userId, UUID followId) {
        userService.getById(userId);

        FollowModel follow = followService.getById(followId);

        if (!follow.getFollowerId().equals(userId)) {
            throw new FollowNotAllowedException(userId, followId);
        }

        UserUnfollowedEvent event = UserUnfollowedEvent.builder()
            .followerId(follow.getFollowerId())
            .followeeId(follow.getFolloweeId())
            .build();

        transactionTemplate.executeWithoutResult(status -> {
            followService.delete(follow);
            outboxService.save(domainEventOutboxMapper.toOutboxModel(event));
        });
    }

    public long getFollowerCount(UUID followeeId) {
        userService.getById(followeeId);
        return followService.getFollowerCount(followeeId);
    }

    public boolean isFollow(UUID followerId, UUID followeeId) {
        userService.getById(followerId);
        userService.getById(followeeId);
        return followService.isFollow(followerId, followeeId);
    }
}
