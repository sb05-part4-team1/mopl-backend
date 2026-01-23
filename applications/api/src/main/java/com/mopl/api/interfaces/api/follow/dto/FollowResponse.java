package com.mopl.api.interfaces.api.follow.dto;

import java.util.UUID;

public record FollowResponse(
    UUID id,
    UUID followeeId,
    UUID followerId
) {}
