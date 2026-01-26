package com.mopl.domain.exception.follow;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FollowErrorCode implements ErrorCode {

    SELF_FOLLOW_NOT_ALLOWED(400, "자기 자신은 팔로우할 수 없습니다."),
    FOLLOW_NOT_ALLOWED(403, "팔로우가 허용되지 않습니다."),
    FOLLOW_NOT_FOUND(404, "팔로우 관계를 찾을 수 없습니다."),
    FOLLOW_ALREADY_EXISTS(409, "이미 팔로우 중입니다.");

    private final int status;
    private final String message;
}
