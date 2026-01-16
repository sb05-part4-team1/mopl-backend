package com.mopl.api.application.playlist;

import com.mopl.api.interfaces.api.playlist.PlaylistCreateRequest;
import com.mopl.api.interfaces.api.playlist.PlaylistUpdateRequest;
import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.playlist.PlaylistService;
import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
import com.mopl.domain.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlaylistFacade {

    private final PlaylistService playlistService;
    private final PlaylistSubscriptionService playlistSubscriptionService;
    private final UserService userService;
    private final ContentService contentService;

    public PlaylistModel createPlaylist(
        UUID requesterId,
        PlaylistCreateRequest request
    ) {
        UserModel owner = userService.getById(requesterId);

        return playlistService.create(
            owner,
            request.title(),
            request.description()
        );
    }

    public PlaylistModel updatePlaylist(
        UUID requesterId,
        UUID playlistId,
        PlaylistUpdateRequest request
    ) {
        userService.getById(requesterId);

        return playlistService.update(
            playlistId,
            requesterId,
            request.title(),
            request.description()
        );
    }

    public void deletePlaylist(
        UUID requesterId,
        UUID playlistId
    ) {
        userService.getById(requesterId);
        playlistService.delete(playlistId, requesterId);
    }

    public PlaylistDetail getPlaylist(UUID requesterId, UUID playlistId) {
        UserModel requester = userService.getById(requesterId);
        PlaylistModel playlist = playlistService.getById(playlistId);
        long subscriberCount = playlistSubscriptionService.getSubscriberCount(playlist.getId());
        boolean subscribedByMe = playlistSubscriptionService.isSubscribedByPlaylistIdAndSubscriberId(
            playlist.getId(),
            requester.getId()
        );
        List<ContentModel> contents = playlistService.getContents(playlist.getId());

        return new PlaylistDetail(playlist, subscriberCount, subscribedByMe, contents);
    }

    @Transactional
    public void addContentToPlaylist(
        UUID requesterId,
        UUID playlistId,
        UUID contentId
    ) {
        userService.getById(requesterId);

        if (!contentService.exists(contentId)) {
            throw ContentNotFoundException.withId(contentId);
        }

        playlistService.addContent(playlistId, requesterId, contentId);
    }

    @Transactional
    public void deleteContentFromPlaylist(
        UUID requesterId,
        UUID playlistId,
        UUID contentId
    ) {
        userService.getById(requesterId);
        playlistService.removeContent(playlistId, requesterId, contentId);
    }

    public void subscribePlaylist(
        UUID requesterId,
        UUID playlistId
    ) {
        userService.getById(requesterId);
        playlistService.getById(playlistId);
        playlistSubscriptionService.subscribe(playlistId, requesterId);
        // TODO: 구독 알림 이벤트 발행
    }

    public void unsubscribePlaylist(
        UUID requesterId,
        UUID playlistId
    ) {
        userService.getById(requesterId);
        playlistService.getById(playlistId);
        playlistSubscriptionService.unsubscribe(playlistId, requesterId);
    }
}
