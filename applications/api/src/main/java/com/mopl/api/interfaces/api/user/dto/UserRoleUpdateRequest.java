package com.mopl.api.interfaces.api.user.dto;

import com.mopl.domain.model.user.UserModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
    description = "사용자 역할 수정 요청",
    example = """
        {
          "role": "ADMIN"
        }
        """
)
public record UserRoleUpdateRequest(
    @Schema(
        description = "할당할 역할",
        example = "ADMIN",
        allowableValues = {
            "USER", "ADMIN"
        }
    ) @NotNull UserModel.Role role
) {
}
