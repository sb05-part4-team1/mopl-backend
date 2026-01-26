package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistContentAlreadyExistsException;
import com.mopl.domain.exception.playlist.PlaylistContentNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.playlist.PlaylistQueryRepository;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.support.cache.CacheName;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistCacheService playlistCacheService;
    private final PlaylistQueryRepository playlistQueryRepository;
    private final PlaylistContentRepository playlistContentRepository;

    public CursorResponse<PlaylistModel> getAll(PlaylistQueryRequest request) {
        return playlistQueryRepository.findAll(request);
    }

    public PlaylistModel getById(UUID playlistId) {
        return playlistCacheService.getById(playlistId);
    }

    public List<ContentModel> getContentsByPlaylistId(UUID playlistId) {
        return playlistCacheService.getContentsByPlaylistId(playlistId);
    }

    public Map<UUID, List<ContentModel>> getContentsByPlaylistIdIn(Collection<UUID> playlistIds) {
        return playlistContentRepository.findContentsByPlaylistIdIn(playlistIds);
    }

    public PlaylistModel create(PlaylistModel playlistModel) {
        return playlistCacheService.save(playlistModel);
    }

    public PlaylistModel update(PlaylistModel playlistModel) {
        return playlistCacheService.save(playlistModel);
    }

    public void delete(PlaylistModel playlistModel) {
        playlistModel.delete();
        playlistCacheService.saveAndEvict(playlistModel);
    }

    @CacheEvict(cacheNames = CacheName.PLAYLIST_CONTENTS, key = "#playlistId")
    public void addContent(UUID playlistId, UUID contentId) {
        if (playlistContentRepository.exists(playlistId, contentId)) {
            throw PlaylistContentAlreadyExistsException.withPlaylistIdAndContentId(playlistId, contentId);
        }
        playlistContentRepository.save(playlistId, contentId);
    }

    @CacheEvict(cacheNames = CacheName.PLAYLIST_CONTENTS, key = "#playlistId")
    public void deleteContentFromPlaylist(UUID playlistId, UUID contentId) {
        boolean deleted = playlistContentRepository.deleteByPlaylistIdAndContentId(playlistId, contentId);
        if (!deleted) {
            throw PlaylistContentNotFoundException.withPlaylistIdAndContentId(playlistId, contentId);
        }
    }
}
