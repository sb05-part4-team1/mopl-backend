package com.mopl.api.interfaces.api.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConversationCreateRequest(
    @Schema(description = "대화 상대 정보 ID", format = "uuid") @NotNull(
        message = "상대 유저의 ID는 필수입니다.") UUID withUserId
) {
}
