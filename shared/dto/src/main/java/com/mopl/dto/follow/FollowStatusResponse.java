package com.mopl.dto.follow;

import java.util.UUID;

public record FollowStatusResponse(
    boolean followed,
    UUID followId
) {
}
