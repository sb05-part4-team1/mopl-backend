package com.mopl.api.interfaces.api.user;

import java.util.UUID;

public record FollowResponse(
    UUID id,
    UUID followeeId,
    UUID followerId
) {
}
