package com.mopl.api.interfaces.api.auth;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
    @NotBlank String email
) {
}
