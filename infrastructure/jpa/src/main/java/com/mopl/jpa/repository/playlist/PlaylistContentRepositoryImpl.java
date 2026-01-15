package com.mopl.jpa.repository.playlist;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.entity.playlist.PlaylistContentEntity;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.repository.content.JpaContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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

        jpaPlaylistContentRepository.save(entity);
    }

    @Override
    public void delete(UUID playlistId, UUID contentId) {
        jpaPlaylistContentRepository.deleteByPlaylistIdAndContentId(playlistId, contentId);
    }
}
