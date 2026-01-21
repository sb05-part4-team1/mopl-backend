package com.mopl.api.interfaces.api.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(

    @Schema(description = "이메일") @NotBlank String email
) {
}
