package com.mopl.jpa.entity.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaylistEntityMapper {

    private final UserEntityMapper userEntityMapper;

    public PlaylistModel toModel(PlaylistEntity playlistEntity) {
        if (playlistEntity == null) {
            return null;
        }

        return buildPlaylistModel(playlistEntity, toOwnerIdOnly(playlistEntity.getOwner()));
    }

    public PlaylistModel toModelWithOwner(PlaylistEntity playlistEntity) {
        if (playlistEntity == null) {
            return null;
        }

        return buildPlaylistModel(playlistEntity, userEntityMapper.toModel(playlistEntity
            .getOwner()));
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
            .owner(userEntityMapper.toEntity(playlistModel.getOwner()))
            .title(playlistModel.getTitle())
            .description(playlistModel.getDescription())
            .build();
    }

    private PlaylistModel buildPlaylistModel(PlaylistEntity playlistEntity, UserModel ownerModel) {
        return PlaylistModel.builder()
            .id(playlistEntity.getId())
            .createdAt(playlistEntity.getCreatedAt())
            .updatedAt(playlistEntity.getUpdatedAt())
            .deletedAt(playlistEntity.getDeletedAt())
            .owner(ownerModel)
            .title(playlistEntity.getTitle())
            .description(playlistEntity.getDescription())
            .build();
    }

    private UserModel toOwnerIdOnly(UserEntity ownerEntity) {
        return ownerEntity != null
            ? UserModel.builder().id(ownerEntity.getId()).build()
            : null;
    }
}
