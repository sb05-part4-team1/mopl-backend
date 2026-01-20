package com.mopl.domain.event.playlist;

import com.mopl.domain.event.AbstractDomainEvent;
import com.mopl.domain.event.EventTopic;
import com.mopl.domain.model.playlist.PlaylistModel;
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
