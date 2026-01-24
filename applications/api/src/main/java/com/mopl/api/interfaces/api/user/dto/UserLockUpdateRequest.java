package com.mopl.api.interfaces.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
    example = """
        {
          "locked": true
        }
        """
)
public record UserLockUpdateRequest(
    @NotNull Boolean locked
) {
}
