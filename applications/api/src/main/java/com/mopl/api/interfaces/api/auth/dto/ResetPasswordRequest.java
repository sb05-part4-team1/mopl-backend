package com.mopl.api.interfaces.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(example = """
    {
      "email": "test@example.com"
    }
    """)
public record ResetPasswordRequest(
    @NotBlank String email
) {}
