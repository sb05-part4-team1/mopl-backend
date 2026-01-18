package com.mopl.jpa.repository.playlist;

import com.mopl.domain.exception.playlist.PlaylistContentAlreadyExistsException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.entity.playlist.PlaylistContentEntity;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.repository.content.JpaContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PlaylistContentRepositoryImpl implements PlaylistContentRepository {

    private final JpaPlaylistContentRepository jpaPlaylistContentRepository;
    private final JpaPlaylistRepository jpaPlaylistRepository;
    private final JpaContentRepository jpaContentRepository;
    private final ContentEntityMapper contentEntityMapper;

    @Override
    public List<ContentModel> findContentsByPlaylistId(UUID playlistId) {
        return jpaPlaylistContentRepository.findByPlaylistId(playlistId).stream()
            .map(PlaylistContentEntity::getContent)
            .map(contentEntityMapper::toModel)
            .toList();
    }

    @Override
    public boolean exists(UUID playlistId, UUID contentId) {
        return jpaPlaylistContentRepository.existsByPlaylistIdAndContentId(playlistId, contentId);
    }

    @Override
    public void save(UUID playlistId, UUID contentId) {
        PlaylistEntity playlistRef = jpaPlaylistRepository.getReferenceById(playlistId);
        ContentEntity contentRef = jpaContentRepository.getReferenceById(contentId);

        PlaylistContentEntity entity = PlaylistContentEntity.builder()
            .playlist(playlistRef)
            .content(contentRef)
            .build();

        try {
            jpaPlaylistContentRepository.save(entity);
        } catch (DataIntegrityViolationException exception) {
            throw new PlaylistContentAlreadyExistsException(playlistId, contentId);
        }
    }

    @Override
    public boolean delete(UUID playlistId, UUID contentId) {
        int deletedCount = jpaPlaylistContentRepository.deleteByPlaylistIdAndContentId(
            playlistId,
            contentId
        );
        return deletedCount > 0;
    }

    @Override
    public Map<UUID, List<ContentModel>> findContentsByPlaylistIds(Collection<UUID> playlistIds) {
        if (playlistIds.isEmpty()) {
            return Map.of();
        }

        List<PlaylistContentEntity> entities = jpaPlaylistContentRepository.findByPlaylistIdIn(
            playlistIds);

        Map<UUID, List<ContentModel>> result = new HashMap<>();
        for (PlaylistContentEntity entity : entities) {
            UUID playlistId = entity.getPlaylist().getId();
            ContentModel contentModel = contentEntityMapper.toModel(entity.getContent());
            result.computeIfAbsent(playlistId, k -> new ArrayList<>()).add(contentModel);
        }

        return result;
    }
}
