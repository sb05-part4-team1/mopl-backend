package com.mopl.domain.repository.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;

import java.util.Optional;
import java.util.UUID;

public interface PlaylistRepository {
    PlaylistModel save(PlaylistModel playlistModel);

    Optional<PlaylistModel> findById(UUID playlistId);
}
