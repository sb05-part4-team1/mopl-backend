package com.mopl.domain.repository.playlist;

import java.util.UUID;

public interface PlaylistContentRepository {

    boolean exists(UUID playlistId, UUID contentId);

    void save(UUID playlistId, UUID contentId);
}
