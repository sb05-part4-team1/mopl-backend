package com.mopl.api.interfaces.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
    description = "계정 잠금 상태 수정 요청",
    example = """
        {
          "locked": true
        }
        """
)
public record UserLockUpdateRequest(
    @Schema(
        description = "계정 잠금 여부 (true: 잠금, false: 잠금 해제)",
        example = "true"
    ) @NotNull Boolean locked
) {
}
