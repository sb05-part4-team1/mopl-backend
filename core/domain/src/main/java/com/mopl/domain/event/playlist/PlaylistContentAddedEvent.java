package com.mopl.domain.event.playlist;

import com.mopl.domain.event.AbstractDomainEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@SuperBuilder
public class PlaylistContentAddedEvent extends AbstractDomainEvent {

    private final UUID playlistId;
    private final String playlistTitle;
    private final UUID ownerId;
    private final String ownerName;
    private final UUID contentId;
    private final String contentTitle;
    private final List<UUID> subscriberIds;

    @Override
    public String getAggregateType() {
        return "PLAYLIST";
    }

    @Override
    public String getAggregateId() {
        return playlistId.toString();
    }
}
