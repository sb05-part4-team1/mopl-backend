package com.mopl.api.application.playlist;

import com.mopl.api.interfaces.api.playlist.PlaylistCreateRequest;
import com.mopl.api.interfaces.api.playlist.PlaylistResponse;
import com.mopl.api.interfaces.api.playlist.PlaylistResponseMapper;
import com.mopl.api.interfaces.api.playlist.PlaylistUpdateRequest;
import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.playlist.PlaylistService;
import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
import com.mopl.domain.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlaylistFacade {

    private final PlaylistService playlistService;
    private final PlaylistSubscriptionService playlistSubscriptionService;
    private final UserService userService;
    private final ContentService contentService;
    private final PlaylistResponseMapper playlistResponseMapper;

    @Transactional
    public PlaylistResponse createPlaylist(
        UUID requesterId,
        PlaylistCreateRequest request
    ) {

        // 요청자(= 플레이리스트 소유자) 조회: 존재하지 않으면 예외 발생
        UserModel owner = userService.getById(requesterId);

        // 플레이리스트 생성 (소유자 요청자 본인으로 고정)
        PlaylistModel savedPlaylist = playlistService.create(
            owner,
            request.title(),
            request.description()
        );

        // 응답 DTO 변환 후 반환
        return playlistResponseMapper.toResponse(savedPlaylist);
    }

    @Transactional
    public PlaylistResponse updatePlaylist(
        UUID requesterId,
        UUID playlistId,
        PlaylistUpdateRequest request
    ) {

        // requester 존재 보장
        userService.getById(requesterId);

        PlaylistModel updatedPlaylist = playlistService.update(
            playlistId,
            requesterId,
            request.title(),
            request.description()
        );

        return playlistResponseMapper.toResponse(
            updatedPlaylist,
            0L, // subscriberCount (추후 구현)
            false,           // subscribedByMe (추후 구현)
            Collections.emptyList()
        );
    }

    @Transactional
    public void deletePlaylist(
        UUID requesterId,
        UUID playlistId
    ) {
        // requester 존재 보장 (ReviewFacade delete와 동일 패턴)
        userService.getById(requesterId);
        playlistService.delete(playlistId, requesterId);
    }

    @Transactional
    public PlaylistResponse getPlaylist(
        UUID requesterId,
        UUID playlistId
    ) {
        userService.getById(requesterId);

        PlaylistModel playlist = playlistService.getById(playlistId);

        return playlistResponseMapper.toResponse(
            playlist,
            0L,  // subscriberCount (추후 구현)
            false,            // subscribedByMe (추후 구현)
            Collections.emptyList()
        );
    }

    // ============= 여기서 부터는 순수 플레이리스트 CRUD가 아님===================

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

    @Transactional
    public void subscribePlaylist(
        UUID requesterId,
        UUID playlistId
    ) {
        userService.getById(requesterId);
        playlistService.getById(playlistId);

        playlistSubscriptionService.subscribe(playlistId, requesterId);

        // TODO: 알림 구현 시 여기(or 서비스 내부)에서 "구독 알림 이벤트" 발행
    }

    @Transactional
    public void unsubscribePlaylist(
        UUID requesterId,
        UUID playlistId
    ) {
        userService.getById(requesterId);
        playlistService.getById(playlistId);

        playlistSubscriptionService.unsubscribe(playlistId, requesterId);
    }
}
