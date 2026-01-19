package com.mopl.domain.event.user;

import com.mopl.domain.event.AbstractDomainEvent;
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
        return "USER";
    }

    @Override
    public String getAggregateId() {
        return userId.toString();
    }
}
