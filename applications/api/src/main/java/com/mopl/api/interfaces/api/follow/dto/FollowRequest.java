package com.mopl.api.interfaces.api.follow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(
    description = "팔로우 요청",
    example = """
        {
          "followeeId": "550e8400-e29b-41d4-a716-446655440000"
        }
        """
)
public record FollowRequest(
    @Schema(
        description = "팔로우할 대상 사용자 ID",
        example = "550e8400-e29b-41d4-a716-446655440000"
    ) @NotNull UUID followeeId
) {
}
