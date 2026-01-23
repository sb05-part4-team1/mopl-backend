package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistContentAlreadyExistsException;
import com.mopl.domain.exception.playlist.PlaylistContentNotFoundException;
import com.mopl.domain.exception.playlist.PlaylistForbiddenException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.playlist.PlaylistQueryRepository;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistCacheService playlistCacheService;
    private final PlaylistQueryRepository playlistQueryRepository;
    private final PlaylistContentRepository playlistContentRepository;

    public CursorResponse<PlaylistModel> getAll(PlaylistQueryRequest request) {
        return playlistQueryRepository.findAll(request);
    }

    public PlaylistModel getById(UUID playlistId) {
        return playlistCacheService.getById(playlistId);
    }

    public List<ContentModel> getContentsByPlaylistId(UUID playlistId) {
        return playlistCacheService.getContentsByPlaylistId(playlistId);
    }

    public Map<UUID, List<ContentModel>> getContentsByPlaylistIdIn(Collection<UUID> playlistIds) {
        return playlistContentRepository.findContentsByPlaylistIdIn(playlistIds);
    }

    public PlaylistModel create(
        String title,
        String description,
        UserModel owner
    ) {
        PlaylistModel playlistModel = PlaylistModel.create(
            title,
            description,
            owner
        );
        return playlistCacheService.save(playlistModel);
    }

    public PlaylistModel update(
        UUID playlistId,
        UUID requesterId,
        String title,
        String description
    ) {
        PlaylistModel playlistModel = getByIdAndValidateOwner(playlistId, requesterId);
        PlaylistModel updatedPlaylist = playlistModel.update(title, description);
        return playlistCacheService.save(updatedPlaylist);
    }

    public void delete(
        UUID playlistId,
        UUID requesterId
    ) {
        PlaylistModel playlistModel = getByIdAndValidateOwner(playlistId, requesterId);
        playlistModel.delete();
        playlistCacheService.saveAndEvict(playlistModel);
    }

    public void addContent(
        UUID playlistId,
        UUID requesterId,
        UUID contentId
    ) {
        getByIdAndValidateOwner(playlistId, requesterId);
        if (playlistContentRepository.exists(playlistId, contentId)) {
            throw PlaylistContentAlreadyExistsException.withPlaylistIdAndContentId(playlistId, contentId);
        }
        playlistContentRepository.save(playlistId, contentId);
        playlistCacheService.evictContents(playlistId);
    }

    public void removeContent(
        UUID playlistId,
        UUID requesterId,
        UUID contentId
    ) {
        getByIdAndValidateOwner(playlistId, requesterId);
        boolean deleted = playlistContentRepository.delete(playlistId, contentId);
        if (!deleted) {
            throw PlaylistContentNotFoundException.withPlaylistIdAndContentId(playlistId, contentId);
        }
        playlistCacheService.evictContents(playlistId);
    }

    private PlaylistModel getByIdAndValidateOwner(UUID playlistId, UUID requesterId) {
        PlaylistModel playlistModel = playlistCacheService.getById(playlistId);
        UUID ownerId = playlistModel.getOwner().getId();

        if (!ownerId.equals(requesterId)) {
            throw PlaylistForbiddenException.withPlaylistIdAndRequesterIdAndOwnerId(
                playlistId,
                requesterId,
                ownerId
            );
        }

        return playlistModel;
    }
}
