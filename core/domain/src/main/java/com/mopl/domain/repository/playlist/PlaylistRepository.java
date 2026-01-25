package com.mopl.domain.repository.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaylistRepository {

    Optional<PlaylistModel> findById(UUID playlistId);

    PlaylistModel save(PlaylistModel playlistModel);

    List<UUID> findCleanupTargets(Instant threshold, int limit);

    int deleteByIdIn(List<UUID> playlistIds);

    void incrementSubscriberCount(UUID playlistId);

    void decrementSubscriberCount(UUID playlistId);
}
