package com.mopl.domain.exception.playlist;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaylistErrorCode implements ErrorCode {

    INVALID_PLAYLIST_DATA(400, "유효하지 않은 플레이리스트 데이터입니다"),
    PLAYLIST_FORBIDDEN(403, "플레이리스트에 대한 권한이 없습니다"),
    PLAYLIST_NOT_FOUND(404, "플레이리스트를 찾을 수 없습니다"),
    PLAYLIST_CONTENT_NOT_FOUND(404, "플레이리스트에 존재하지 않는 콘텐츠입니다"),
    PLAYLIST_CONTENT_ALREADY_EXISTS(409, "이미 플레이리스트에 존재하는 콘텐츠입니다");

    private final int status;
    private final String message;
}
