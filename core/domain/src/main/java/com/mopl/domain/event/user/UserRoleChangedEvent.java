package com.mopl.domain.event.user;

import com.mopl.domain.event.AbstractDomainEvent;
import com.mopl.domain.event.EventTopic;
import com.mopl.domain.model.user.UserModel;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
public class UserRoleChangedEvent extends AbstractDomainEvent {

    private final UUID userId;
    private final String oldRole;
    private final String newRole;

    @Override
    public String getAggregateType() {
        return UserModel.class.getSimpleName();
    }

    @Override
    public String getAggregateId() {
        return userId.toString();
    }

    @Override
    public String getTopic() {
        return EventTopic.USER_ROLE_CHANGED;
    }
}
