package com.mopl.domain.exception.follow;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class FollowNotAllowedException extends FollowException {

    private static final ErrorCode ERROR_CODE = FollowErrorCode.FOLLOW_NOT_ALLOWED;

    private FollowNotAllowedException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static FollowNotAllowedException withRequesterIdAndFollowId(
        UUID requesterId,
        UUID followId
    ) {
        return new FollowNotAllowedException(
            Map.of(
                "requesterId", requesterId,
                "followId", followId
            ));
    }
}
