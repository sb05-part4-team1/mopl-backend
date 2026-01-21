package com.mopl.api.interfaces.api.follow;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record FollowResponse(

    @Schema(description = "팔로우 ID", format = "double") UUID id,

    @Schema(description = "팔로우 대상 사용자 ID", format = "uuid") UUID followeeId,

    @Schema(description = "팔로워 사용자 ID", format = "uuid") UUID followerId
) {
}
