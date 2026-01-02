package com.mopl.domain.exception.follow;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FollowErrorCode implements ErrorCode {

    SELF_FOLLOW_NOT_ALLOWED(400, "자기 자신은 팔로우할 수 없습니다"),
    FOLLOW_NOT_ALLOWED(403, "이미 사용 중인 이메일입니다"),
    FOLLOW_NOT_FOUND(404, "존재하지 않거나 이미 취소된 팔로우 관계입니다");

    private final int status;
    private final String message;
}
