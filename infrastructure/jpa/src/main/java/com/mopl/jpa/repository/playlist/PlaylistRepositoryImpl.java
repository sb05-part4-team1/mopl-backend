package com.mopl.jpa.repository.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.entity.playlist.PlaylistEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PlaylistRepositoryImpl implements PlaylistRepository {

    private final JpaPlaylistRepository jpaPlaylistRepository;
    private final PlaylistEntityMapper playlistEntityMapper;

    @Override
    public Optional<PlaylistModel> findById(UUID playlistId) {
        return jpaPlaylistRepository.findWithOwnerById(playlistId)
            .map(playlistEntityMapper::toModelWithOwner);
    }

    @Override
    public PlaylistModel save(PlaylistModel playlistModel) {
        PlaylistEntity playlistEntity = playlistEntityMapper.toEntity(playlistModel);
        PlaylistEntity savedPlaylistEntity = jpaPlaylistRepository.save(playlistEntity);
        return playlistEntityMapper.toModelWithOwner(savedPlaylistEntity);
    }
}
