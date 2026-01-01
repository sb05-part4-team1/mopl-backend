package com.mopl.api.interfaces.api.user;

import com.mopl.domain.model.user.UserModel;
import io.swagger.v3.oas.annotations.media.Schema;

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
