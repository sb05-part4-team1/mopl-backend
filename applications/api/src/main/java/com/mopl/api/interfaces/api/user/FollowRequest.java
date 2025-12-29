package com.mopl.api.interfaces.api.user;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record FollowRequest(
    @NotNull(message = "팔로우할 사용자 ID는 필수입니다.") UUID followeeId
) {
}
