package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistForbiddenException;
import com.mopl.domain.exception.playlist.PlaylistNotFoundException;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public PlaylistModel create(
        UserModel owner,
        String title,
        String description
    ) {
        PlaylistModel playlistModel = PlaylistModel.create(
            owner.getId(),
            title,
            description
        );

        return playlistRepository.save(playlistModel);

    }

    public PlaylistModel update(
        UUID playlistId,
        UUID requesterId,
        String title,
        String description
    ) {
        PlaylistModel playlistModel = playlistRepository.findById(playlistId)
            .orElseThrow(() -> new PlaylistNotFoundException(playlistId));

        if (!playlistModel.getOwnerId().equals(requesterId)) {
            throw new PlaylistForbiddenException(
                playlistId,
                requesterId,
                playlistModel.getOwnerId()
            );
        }

        playlistModel.update(title, description);

        return playlistRepository.save(playlistModel);
    }
}
