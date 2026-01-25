package com.mopl.jpa.repository.playlist.projection;

import java.util.UUID;

public interface PlaylistSubscriberCountProjection {

    UUID getPlaylistId();

    Long getSubscriberCount();
}
