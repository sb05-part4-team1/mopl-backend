package com.mopl.api.interfaces.api.conversation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(example = """
    {
      "withUserId": "550e8400-e29b-41d4-a716-446655440000"
    }
    """)
public record ConversationCreateRequest(
    @NotNull(message = "상대 유저의 ID는 필수입니다.") UUID withUserId
) {
}
