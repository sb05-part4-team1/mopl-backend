package com.mopl.api.interfaces.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.mopl.domain.model.user.UserModel.RAW_PASSWORD_MAX_LENGTH;

@Schema(
    example = """
        {
          "password": "P@ssw0rd!"
        }
        """
)
public record ChangePasswordRequest(
    @NotBlank @Size(max = RAW_PASSWORD_MAX_LENGTH) String password
) {
}
