package com.mopl.api.interfaces.api.conversation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(
    description = "대화 생성 요청",
    example = """
        {
          "withUserId": "550e8400-e29b-41d4-a716-446655440000"
        }
        """
)
public record ConversationCreateRequest(
    @Schema(description = "대화를 시작할 상대 사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull UUID withUserId
) {
}
