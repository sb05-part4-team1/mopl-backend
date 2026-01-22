package com.mopl.domain.event.playlist;

import com.mopl.domain.event.DomainEvent;
import com.mopl.domain.event.EventTopic;
import com.mopl.domain.model.playlist.PlaylistModel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Getter
@Builder
@Jacksonized
public class PlaylistUnsubscribedEvent implements DomainEvent {

    private final UUID playlistId;
    private final UUID subscriberId;

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
        return EventTopic.PLAYLIST_UNSUBSCRIBED;
    }
}
