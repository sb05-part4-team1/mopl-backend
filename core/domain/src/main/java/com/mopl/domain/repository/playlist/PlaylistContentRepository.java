package com.mopl.domain.repository.playlist;

import com.mopl.domain.model.content.ContentModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PlaylistContentRepository {

    List<ContentModel> findContentsByPlaylistId(UUID playlistId);

    Map<UUID, List<ContentModel>> findContentsByPlaylistIds(Collection<UUID> playlistIds);

    boolean exists(UUID playlistId, UUID contentId);

    void save(UUID playlistId, UUID contentId);

    boolean delete(UUID playlistId, UUID contentId);

    // 이하 메서드들 cleanup batch 전용
    int deleteAllByContentIds(List<UUID> contentIds);

    int deleteAllByPlaylistIds(List<UUID> playlistIds);
}
