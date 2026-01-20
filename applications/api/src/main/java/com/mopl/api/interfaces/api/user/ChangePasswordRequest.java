package com.mopl.api.interfaces.api.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.mopl.domain.model.user.UserModel.RAW_PASSWORD_MAX_LENGTH;

public record ChangePasswordRequest(
    @NotBlank @Size(max = RAW_PASSWORD_MAX_LENGTH) String password
) {
}
