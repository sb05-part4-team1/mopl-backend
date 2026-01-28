package com.mopl.api.interfaces.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
    description = "비밀번호 재설정 요청",
    example = """
        {
          "email": "test@example.com"
        }
        """
)
public record ResetPasswordRequest(
    @Schema(
        description = "비밀번호를 재설정할 사용자 이메일",
        example = "test@example.com"
    ) @NotBlank String email
) {
}
