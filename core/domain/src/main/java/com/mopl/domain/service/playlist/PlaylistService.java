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
            owner,
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

        UUID ownerId = (playlistModel.getOwner() != null) ? playlistModel.getOwner().getId() : null;

        if (ownerId == null || !ownerId.equals(requesterId)) {
            throw new PlaylistForbiddenException(
                playlistId,
                requesterId,
                ownerId
            );
        }

        playlistModel.update(title, description);

        return playlistRepository.save(playlistModel);
    }

    public void delete(
        UUID playlistid,
        UUID requesterid
    ) {
        PlaylistModel playlistModel = playlistRepository.findById(playlistid)
            .orElseThrow(() -> new PlaylistNotFoundException(playlistid));

        UUID ownerId = (playlistModel.getOwner() != null) ? playlistModel.getOwner().getId() : null;

        if (ownerId == null || !ownerId.equals(requesterid)) {
            throw new PlaylistForbiddenException(playlistid, requesterid, ownerId);
        }

        playlistModel.deletePlaylist();

        playlistRepository.save(playlistModel);
    }
}
