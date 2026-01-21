package com.mopl.api.application.playlist;

import com.mopl.api.application.outbox.DomainEventOutboxMapper;
import com.mopl.api.interfaces.api.playlist.PlaylistCreateRequest;
import com.mopl.api.interfaces.api.playlist.PlaylistResponse;
import com.mopl.api.interfaces.api.playlist.PlaylistResponseMapper;
import com.mopl.api.interfaces.api.playlist.PlaylistUpdateRequest;
import com.mopl.domain.event.playlist.PlaylistContentAddedEvent;
import com.mopl.domain.event.playlist.PlaylistSubscribedEvent;
import com.mopl.domain.event.playlist.PlaylistUnsubscribedEvent;
import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.outbox.OutboxService;
import com.mopl.domain.service.playlist.PlaylistService;
import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlaylistFacade {

    private final PlaylistService playlistService;
    private final PlaylistSubscriptionService playlistSubscriptionService;
    private final UserService userService;
    private final ContentService contentService;
    private final PlaylistResponseMapper playlistResponseMapper;
    private final DomainEventOutboxMapper domainEventOutboxMapper;
    private final OutboxService outboxService;
    private final TransactionTemplate transactionTemplate;

    public CursorResponse<PlaylistResponse> getPlaylists(
        UUID requesterId,
        PlaylistQueryRequest request
    ) {
        CursorResponse<PlaylistModel> playlistPage = playlistService.getAll(request);
        List<PlaylistModel> playlists = playlistPage.data();

        if (playlists.isEmpty()) {
            return playlistPage.map(playlistResponseMapper::toResponse);
        }

        List<UUID> playlistIds = playlists.stream()
            .map(PlaylistModel::getId)
            .toList();

        Map<UUID, Long> subscriberCounts = playlistSubscriptionService.getSubscriberCounts(
            playlistIds);
        Set<UUID> subscribedPlaylistIds = playlistSubscriptionService.findSubscribedPlaylistIds(
            requesterId,
            playlistIds
        );
        Map<UUID, List<ContentModel>> contentsMap = playlistService.getContentsByPlaylistIds(
            playlistIds);

        return playlistPage.map(playlist -> playlistResponseMapper.toResponse(
            playlist,
            subscriberCounts.getOrDefault(playlist.getId(), 0L),
            subscribedPlaylistIds.contains(playlist.getId()),
            contentsMap.getOrDefault(playlist.getId(), Collections.emptyList())
        ));
    }

    public PlaylistResponse getPlaylist(UUID requesterId, UUID playlistId) {
        UserModel requester = userService.getById(requesterId);
        PlaylistModel playlist = playlistService.getById(playlistId);
        long subscriberCount = playlistSubscriptionService.getSubscriberCount(playlist.getId());
        boolean subscribedByMe = playlistSubscriptionService
            .isSubscribedByPlaylistIdAndSubscriberId(
                playlist.getId(),
                requester.getId()
            );
        List<ContentModel> contents = playlistService.getContentsByPlaylistId(playlist.getId());

        return playlistResponseMapper.toResponse(
            playlist,
            subscriberCount,
            subscribedByMe,
            contents
        );
    }

    public PlaylistResponse createPlaylist(
        UUID requesterId,
        PlaylistCreateRequest request
    ) {
        UserModel owner = userService.getById(requesterId);

        PlaylistModel playlistModel = playlistService.create(
            owner,
            request.title(),
            request.description()
        );

        return playlistResponseMapper.toResponse(playlistModel);
    }

    public PlaylistResponse updatePlaylist(
        UUID requesterId,
        UUID playlistId,
        PlaylistUpdateRequest request
    ) {
        userService.getById(requesterId);
        PlaylistModel playlistModel = playlistService.update(
            playlistId,
            requesterId,
            request.title(),
            request.description()
        );
        return playlistResponseMapper.toResponse(playlistModel);
    }

    public void deletePlaylist(
        UUID requesterId,
        UUID playlistId
    ) {
        userService.getById(requesterId);
        playlistService.delete(playlistId, requesterId);
    }

    public void addContentToPlaylist(
        UUID requesterId,
        UUID playlistId,
        UUID contentId
    ) {
        UserModel owner = userService.getById(requesterId);

        if (!contentService.exists(contentId)) {
            throw ContentNotFoundException.withId(contentId);
        }

        PlaylistModel playlist = playlistService.getById(playlistId);
        ContentModel content = contentService.getById(contentId);

        PlaylistContentAddedEvent event = PlaylistContentAddedEvent.builder()
            .playlistId(playlist.getId())
            .playlistTitle(playlist.getTitle())
            .ownerId(owner.getId())
            .ownerName(owner.getName())
            .contentId(content.getId())
            .contentTitle(content.getTitle())
            .subscriberIds(List.of())
            .build();

        transactionTemplate.executeWithoutResult(status -> {
            playlistService.addContent(playlistId, requesterId, contentId);
            outboxService.save(domainEventOutboxMapper.toOutboxModel(event));
        });
    }

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
        UserModel subscriber = userService.getById(requesterId);
        PlaylistModel playlist = playlistService.getById(playlistId);

        PlaylistSubscribedEvent event = PlaylistSubscribedEvent.builder()
            .playlistId(playlist.getId())
            .playlistTitle(playlist.getTitle())
            .subscriberId(subscriber.getId())
            .subscriberName(subscriber.getName())
            .ownerId(playlist.getOwner().getId())
            .build();

        transactionTemplate.executeWithoutResult(status -> {
            playlistSubscriptionService.subscribe(playlistId, requesterId);
            outboxService.save(domainEventOutboxMapper.toOutboxModel(event));
        });
    }

    public void unsubscribePlaylist(
        UUID requesterId,
        UUID playlistId
    ) {
        userService.getById(requesterId);
        playlistService.getById(playlistId);

        PlaylistUnsubscribedEvent event = PlaylistUnsubscribedEvent.builder()
            .playlistId(playlistId)
            .subscriberId(requesterId)
            .build();

        transactionTemplate.executeWithoutResult(status -> {
            playlistSubscriptionService.unsubscribe(playlistId, requesterId);
            outboxService.save(domainEventOutboxMapper.toOutboxModel(event));
        });
    }
}
