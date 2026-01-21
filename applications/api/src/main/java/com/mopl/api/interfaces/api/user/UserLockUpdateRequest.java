package com.mopl.api.interfaces.api.user;

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
    @Schema(description = "변경할 잠금 상태") @NotNull Boolean locked
) {
}
