package com.mopl.api.interfaces.api.follow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(
    example = """
        {
          "followeeId": "550e8400-e29b-41d4-a716-446655440000"
        }
        """
)
public record FollowRequest(
    @NotNull UUID followeeId
) {
}
