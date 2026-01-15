package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistContentAlreadyExistsException;
import com.mopl.domain.exception.playlist.PlaylistContentNotFoundException;
import com.mopl.domain.exception.playlist.PlaylistForbiddenException;
import com.mopl.domain.exception.playlist.PlaylistNotFoundException;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistContentRepository playlistContentRepository;

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

    public PlaylistModel getById(UUID playlistId) {
        return playlistRepository.findById(playlistId)
            .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
    }

    public PlaylistModel update(
        UUID playlistId,
        UUID requesterId,
        String title,
        String description
    ) {
        PlaylistModel playlistModel = getByIdAndValidateOwner(playlistId, requesterId);
        playlistModel.update(title, description);
        return playlistRepository.save(playlistModel);
    }

    public void delete(
        UUID playlistId,
        UUID requesterId
    ) {
        PlaylistModel playlistModel = getByIdAndValidateOwner(playlistId, requesterId);
        playlistModel.delete();
        playlistRepository.save(playlistModel);
    }

    public void addContent(
        UUID playlistId,
        UUID requesterId,
        UUID contentId
    ) {
        getByIdAndValidateOwner(playlistId, requesterId);

        if (playlistContentRepository.exists(playlistId, contentId)) {
            throw new PlaylistContentAlreadyExistsException(playlistId, contentId);
        }

        playlistContentRepository.save(playlistId, contentId);
    }

    public void removeContent(
        UUID playlistId,
        UUID requesterId,
        UUID contentId
    ) {
        getByIdAndValidateOwner(playlistId, requesterId);

        if (!playlistContentRepository.exists(playlistId, contentId)) {
            throw new PlaylistContentNotFoundException(playlistId, contentId);
        }

        playlistContentRepository.delete(playlistId, contentId);
    }

    private PlaylistModel getByIdAndValidateOwner(UUID playlistId, UUID requesterId) {
        PlaylistModel playlistModel = getById(playlistId);
        UUID ownerId = (playlistModel.getOwner() != null) ? playlistModel.getOwner().getId() : null;

        if (ownerId == null || !ownerId.equals(requesterId)) {
            throw new PlaylistForbiddenException(playlistId, requesterId, ownerId);
        }

        return playlistModel;
    }
}
