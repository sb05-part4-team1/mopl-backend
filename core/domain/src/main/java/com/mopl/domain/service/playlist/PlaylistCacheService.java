package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.support.cache.CacheName;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PlaylistCacheService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistContentRepository playlistContentRepository;

    @Cacheable(cacheNames = CacheName.PLAYLISTS, key = "#playlistId")
    public PlaylistModel getById(UUID playlistId) {
        return playlistRepository.findById(playlistId)
            .orElseThrow(() -> PlaylistNotFoundException.withId(playlistId));
    }

    @Cacheable(cacheNames = CacheName.PLAYLIST_CONTENTS, key = "#playlistId")
    public List<ContentModel> getContentsByPlaylistId(UUID playlistId) {
        return playlistContentRepository.findContentsByPlaylistId(playlistId);
    }

    @CachePut(cacheNames = CacheName.PLAYLISTS, key = "#result.id")
    public PlaylistModel save(PlaylistModel playlistModel) {
        return playlistRepository.save(playlistModel);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = CacheName.PLAYLISTS, key = "#playlistModel.id"),
        @CacheEvict(cacheNames = CacheName.PLAYLIST_CONTENTS, key = "#playlistModel.id")
    })
    public void saveAndEvict(PlaylistModel playlistModel) {
        playlistRepository.save(playlistModel);
    }
}
