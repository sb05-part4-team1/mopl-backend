package com.mopl.api.interfaces.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.mopl.domain.model.user.UserModel.RAW_PASSWORD_MAX_LENGTH;

@Schema(
    description = "비밀번호 변경 요청",
    example = """
        {
          "password": "P@ssw0rd!"
        }
        """
)
public record ChangePasswordRequest(
    @Schema(description = "새 비밀번호", example = "P@ssw0rd!", maxLength = 72)
    @NotBlank @Size(max = RAW_PASSWORD_MAX_LENGTH) String password
) {
}
