package com.mopl.domain.repository.playlist;

import com.mopl.domain.model.content.ContentModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PlaylistContentRepository {

    List<ContentModel> findContentsByPlaylistId(UUID playlistId);

    Map<UUID, List<ContentModel>> findContentsByPlaylistIdIn(Collection<UUID> playlistIds);

    boolean exists(UUID playlistId, UUID contentId);

    void save(UUID playlistId, UUID contentId);

    boolean delete(UUID playlistId, UUID contentId);
}
