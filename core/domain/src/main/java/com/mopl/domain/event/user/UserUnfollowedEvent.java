package com.mopl.domain.event.user;

import com.mopl.domain.event.AbstractDomainEvent;
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
        return "USER";
    }

    @Override
    public String getAggregateId() {
        return followeeId.toString();
    }
}
