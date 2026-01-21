package com.mopl.api.interfaces.api.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.mopl.domain.model.user.UserModel.RAW_PASSWORD_MAX_LENGTH;

public record ChangePasswordRequest(
    @Schema(description = "새 비밀번호") @NotBlank @Size(max = RAW_PASSWORD_MAX_LENGTH) String password
) {
}
