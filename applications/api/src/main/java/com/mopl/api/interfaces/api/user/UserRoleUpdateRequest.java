package com.mopl.api.interfaces.api.user;

import com.mopl.domain.model.user.UserModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
    example = """
        {
          "role": "ADMIN"
        }
        """
)
public record UserRoleUpdateRequest(
    @NotNull UserModel.Role role
) {
}
