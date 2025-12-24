package com.mopl.api.interfaces.api.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.mopl.domain.model.user.UserModel.EMAIL_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.NAME_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.RAW_PASSWORD_MAX_LENGTH;

@Schema(
    example = """
        {
          "name": "test",
          "email": "test@example.com",
          "password": "P@ssw0rd!"
        }
        """
)
public record UserCreateRequest(
    @NotBlank
    @Size(max = NAME_MAX_LENGTH)
    String name,
    @NotBlank
    @Size(max = EMAIL_MAX_LENGTH)
    String email,
    @NotBlank
    @Size(max = RAW_PASSWORD_MAX_LENGTH)
    String password
) {
}
