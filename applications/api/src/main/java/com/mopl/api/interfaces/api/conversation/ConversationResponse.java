package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.interfaces.api.user.UserSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record ConversationResponse(
    @Schema(description = "대화 ID", format = "uuid") UUID id,

    @Schema(description = "대화 상대 정보") UserSummary with,

    @Schema(description = "마지막 메시지 내용") DirectMessageResponse lastMessage,

    @Schema(description = "읽지 않은 메시지 존재 여부") boolean hasUnread
) {
}
