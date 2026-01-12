package com.mopl.jpa.repository.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.entity.playlist.PlaylistEntityMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PlaylistRepositoryImpl implements PlaylistRepository {

    private final JpaPlaylistRepository jpaPlaylistRepository;
    private final PlaylistEntityMapper playlistEntityMapper;
    private final EntityManager entityManager;

    @Override
    public PlaylistModel save(PlaylistModel playlistModel) {
        PlaylistEntity playlistEntity = playlistEntityMapper.toEntity(playlistModel);
        PlaylistEntity saved = jpaPlaylistRepository.save(playlistEntity);

        // 1) INSERT/UPDATE를 DB에 반영
        entityManager.flush();

        // 2) 1차 캐시 비우기 (그래야 fetch join이 실제로 다시 로딩됨)
        entityManager.clear();

        // 3) owner를 fetch join으로 다시 로딩
        PlaylistEntity savedWithOwner = jpaPlaylistRepository.findByIdWithOwner(saved.getId())
                .orElse(saved);
        return playlistEntityMapper.toModel(savedWithOwner);

    }

    @Override
    public Optional<PlaylistModel> findById(UUID playlistId) {
        return jpaPlaylistRepository.findByIdWithOwner(playlistId)
            .map(playlistEntityMapper::toModel);
    }
}
