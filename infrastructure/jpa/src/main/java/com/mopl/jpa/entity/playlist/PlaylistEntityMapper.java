package com.mopl.jpa.entity.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaylistEntityMapper {

    private final UserEntityMapper userEntityMapper;
    private final EntityManager entityManager;

    public PlaylistModel toModel(PlaylistEntity playlistEntity) {
        if (playlistEntity == null) {
            return null;
        }

        return PlaylistModel.builder()
            .id(playlistEntity.getId())
            .createdAt(playlistEntity.getCreatedAt())
            .updatedAt(playlistEntity.getUpdatedAt())
            .deletedAt(playlistEntity.getDeletedAt())
            .owner(userEntityMapper.toModelIdOnly(playlistEntity.getOwner()))
            .title(playlistEntity.getTitle())
            .description(playlistEntity.getDescription())
            .build();
    }

    public PlaylistModel toModelWithOwner(PlaylistEntity playlistEntity) {
        if (playlistEntity == null) {
            return null;
        }

        return PlaylistModel.builder()
            .id(playlistEntity.getId())
            .createdAt(playlistEntity.getCreatedAt())
            .updatedAt(playlistEntity.getUpdatedAt())
            .deletedAt(playlistEntity.getDeletedAt())
            .owner(userEntityMapper.toModel(playlistEntity.getOwner()))
            .title(playlistEntity.getTitle())
            .description(playlistEntity.getDescription())
            .build();
    }

    public PlaylistEntity toEntity(PlaylistModel playlistModel) {
        if (playlistModel == null) {
            return null;
        }

        return PlaylistEntity.builder()
            .id(playlistModel.getId())
            .createdAt(playlistModel.getCreatedAt())
            .updatedAt(playlistModel.getUpdatedAt())
            .deletedAt(playlistModel.getDeletedAt())
            .owner(entityManager.getReference(UserEntity.class, playlistModel.getOwner().getId()))
            .title(playlistModel.getTitle())
            .description(playlistModel.getDescription())
            .build();
    }
}
