package com.mopl.domain.repository.playlist;

import com.mopl.domain.model.content.ContentModel;

import java.util.List;
import java.util.UUID;

public interface PlaylistContentRepository {

    List<ContentModel> findContentsByPlaylistId(UUID playlistId);

    boolean exists(UUID playlistId, UUID contentId);

    void save(UUID playlistId, UUID contentId);

    void delete(UUID playlistId, UUID contentId);
}
