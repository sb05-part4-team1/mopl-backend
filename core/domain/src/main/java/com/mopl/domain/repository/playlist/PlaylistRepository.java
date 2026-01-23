package com.mopl.domain.repository.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaylistRepository {

    PlaylistModel save(PlaylistModel playlistModel);

    Optional<PlaylistModel> findById(UUID playlistId);

    // 이하 메서드들 cleanup batch 전용
    List<UUID> findCleanupTargets(Instant threshold, int limit);

    int deleteAllByIds(List<UUID> playlistIds);
}
