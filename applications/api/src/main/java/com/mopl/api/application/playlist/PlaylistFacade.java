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
        UUID requsterId,
        UUID playlistId,
        UUID contentId
    ) {
        userService.getById(requsterId);

        if (!contentService.exists(contentId)) {
            throw ContentNotFoundException.withId(contentId);
        }

        playlistService.addContent(playlistId, requsterId, contentId);
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

}
