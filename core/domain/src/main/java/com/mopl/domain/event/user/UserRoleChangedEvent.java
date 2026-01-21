package com.mopl.domain.event.user;

import com.mopl.domain.event.DomainEvent;
import com.mopl.domain.event.EventTopic;
import com.mopl.domain.model.user.UserModel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserRoleChangedEvent implements DomainEvent {

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
