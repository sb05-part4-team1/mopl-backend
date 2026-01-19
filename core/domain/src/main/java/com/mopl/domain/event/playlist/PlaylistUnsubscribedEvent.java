package com.mopl.domain.event.playlist;

import com.mopl.domain.event.AbstractDomainEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
public class PlaylistUnsubscribedEvent extends AbstractDomainEvent {

    private final UUID playlistId;
    private final UUID subscriberId;

    @Override
    public String getAggregateType() {
        return "PLAYLIST";
    }

    @Override
    public String getAggregateId() {
        return playlistId.toString();
    }
}
