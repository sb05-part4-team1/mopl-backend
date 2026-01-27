package com.mopl.api.application.follow;

import com.mopl.domain.event.user.UserFollowedEvent;
import com.mopl.domain.event.user.UserUnfollowedEvent;
import com.mopl.domain.exception.follow.FollowNotAllowedException;
import com.mopl.domain.model.follow.FollowModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.outbox.OutboxService;
import com.mopl.domain.service.user.UserService;
import com.mopl.dto.follow.FollowResponse;
import com.mopl.dto.follow.FollowResponseMapper;
import com.mopl.dto.follow.FollowStatusResponse;
import com.mopl.dto.outbox.DomainEventOutboxMapper;
import com.mopl.logging.context.LogContext;
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
    private final FollowResponseMapper followResponseMapper;
    private final TransactionTemplate transactionTemplate;

    public FollowResponse follow(UUID followerId, UUID followeeId) {
        UserModel follower = userService.getById(followerId);
        UserModel followee = userService.getById(followeeId);

        FollowModel followModel = FollowModel.create(followee.getId(), follower.getId());
        UserFollowedEvent event = UserFollowedEvent.builder()
            .followerId(follower.getId())
            .followerName(follower.getName())
            .followeeId(followee.getId())
            .build();

        return transactionTemplate.execute(status -> {
            FollowModel saved = followService.create(followModel);
            outboxService.save(domainEventOutboxMapper.toOutboxModel(event));

            LogContext.with("followId", saved.getId())
                .and("followerId", followerId)
                .and("followeeId", followeeId)
                .info("User followed");

            return followResponseMapper.toResponse(saved);
        });
    }

    public void unFollow(UUID userId, UUID followId) {
        userService.getById(userId);

        FollowModel follow = followService.getById(followId);

        if (!follow.getFollowerId().equals(userId)) {
            throw FollowNotAllowedException.withRequesterIdAndFollowId(userId, followId);
        }

        UserUnfollowedEvent event = UserUnfollowedEvent.builder()
            .followerId(follow.getFollowerId())
            .followeeId(follow.getFolloweeId())
            .build();

        transactionTemplate.executeWithoutResult(status -> {
            followService.delete(follow);
            outboxService.save(domainEventOutboxMapper.toOutboxModel(event));

            LogContext.with("followId", followId)
                .and("followerId", userId)
                .and("followeeId", follow.getFolloweeId())
                .info("User unfollowed");
        });
    }

    public long getFollowerCount(UUID followeeId) {
        userService.getById(followeeId);
        return followService.getFollowerCount(followeeId);
    }

    public FollowStatusResponse getFollowStatus(UUID followerId, UUID followeeId) {
        userService.getById(followerId);
        userService.getById(followeeId);
        return followService.getByFollowerIdAndFolloweeId(followerId, followeeId)
            .map(follow -> new FollowStatusResponse(true, follow.getId()))
            .orElse(new FollowStatusResponse(false, null));
    }
}
