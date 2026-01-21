package com.mopl.domain.event.user;

import com.mopl.domain.event.DomainEvent;
import com.mopl.domain.event.EventTopic;
import com.mopl.domain.model.user.UserModel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserFollowedEvent implements DomainEvent {

    private final UUID followerId;
    private final String followerName;
    private final UUID followeeId;

    @Override
    public String getAggregateType() {
        return UserModel.class.getSimpleName();
    }

    @Override
    public String getAggregateId() {
        return followeeId.toString();
    }

    @Override
    public String getTopic() {
        return EventTopic.USER_FOLLOWED;
    }
}
