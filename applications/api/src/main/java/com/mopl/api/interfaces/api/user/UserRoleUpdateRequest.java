package com.mopl.api.interfaces.api.user;

import com.mopl.domain.model.user.UserModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.mopl.domain.model.user.UserModel.EMAIL_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.NAME_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.RAW_PASSWORD_MAX_LENGTH;

@Schema(
    example = """
        {
          "role": "ADMIN"
        }
        """
)
public record UserRoleUpdateRequest(
    UserModel.Role role
) {
}
