package com.mopl.domain.event.playlist;

import com.mopl.domain.event.DomainEvent;
import com.mopl.domain.event.EventTopic;
import com.mopl.domain.model.playlist.PlaylistModel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PlaylistUpdatedEvent implements DomainEvent {

    private final UUID playlistId;
    private final String playlistTitle;
    private final UUID ownerId;
    private final String ownerName;

    @Override
    public String getAggregateType() {
        return PlaylistModel.class.getSimpleName();
    }

    @Override
    public String getAggregateId() {
        return playlistId.toString();
    }

    @Override
    public String getTopic() {
        return EventTopic.PLAYLIST_UPDATED;
    }
}
