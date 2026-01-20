package com.mopl.domain.event.user;

import com.mopl.domain.event.AbstractDomainEvent;
import com.mopl.domain.event.EventTopic;
import com.mopl.domain.model.user.UserModel;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
public class UserUnfollowedEvent extends AbstractDomainEvent {

    private final UUID followerId;
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
        return EventTopic.USER_UNFOLLOWED;
    }
}
