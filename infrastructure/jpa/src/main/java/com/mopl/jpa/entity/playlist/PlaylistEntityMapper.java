package com.mopl.jpa.entity.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.jpa.entity.user.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PlaylistEntityMapper {

    public PlaylistModel toModel(PlaylistEntity playlistEntity) {
        if (playlistEntity == null) {
            return null;
        }

        return PlaylistModel.builder()
                .id(playlistEntity.getId())
                .createdAt(playlistEntity.getCreatedAt())
                .updatedAt(playlistEntity.getUpdatedAt())
                .deletedAt(playlistEntity.getDeletedAt())
                .ownerId(
                        playlistEntity.getOwner() != null
                                ? playlistEntity.getOwner().getId()
                                : null
                )
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
                .owner(toOwnerEntity(playlistModel.getOwnerId()))
                .title(playlistModel.getTitle())
                .description(playlistModel.getDescription())
                .build();
    }

    private UserEntity toOwnerEntity(UUID ownerId) {
        if (ownerId == null) {
            return null;
        }

        return UserEntity.builder()
                .id(ownerId)
                .build();
    }
}
