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

        UserModel ownerModel = userEntityMapper.toModel(playlistEntity.getOwner());

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

    public PlaylistEntity toEntity(PlaylistModel playlistModel) {
        if (playlistModel == null) {
            return null;
        }

        return PlaylistEntity.builder()
            .id(playlistModel.getId())
            .createdAt(playlistModel.getCreatedAt())
            .updatedAt(playlistModel.getUpdatedAt())
            .deletedAt(playlistModel.getDeletedAt())
            .owner(toOwnerEntity(playlistModel.getOwner()))
            .title(playlistModel.getTitle())
            .description(playlistModel.getDescription())
            .build();
    }

    private UserEntity toOwnerEntity(UserModel owner) {
        if (owner == null) {
            return null;
        }

        return UserEntity.builder()
            .id(owner.getId())
            .build();
    }
}
