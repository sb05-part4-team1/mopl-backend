package com.mopl.api.interfaces.api.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import static com.mopl.domain.model.user.UserModel.NAME_MAX_LENGTH;

@Schema(
    example = """
        {
          "name": "홍길동"
        }
        """
)
public record UserUpdateRequest(
    @Schema(description = "이름") @Size(max = NAME_MAX_LENGTH) String name
) {
}
