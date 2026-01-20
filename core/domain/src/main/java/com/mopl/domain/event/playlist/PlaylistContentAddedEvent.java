package com.mopl.domain.event.playlist;

import com.mopl.domain.event.AbstractDomainEvent;
import com.mopl.domain.event.EventTopic;
import com.mopl.domain.model.playlist.PlaylistModel;
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
        return PlaylistModel.class.getSimpleName();
    }

    @Override
    public String getAggregateId() {
        return playlistId.toString();
    }

    @Override
    public String getTopic() {
        return EventTopic.PLAYLIST_CONTENT_ADDED;
    }
}
