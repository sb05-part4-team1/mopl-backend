package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.interfaces.api.user.dto.UserSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record DirectMessageResponse(
    @Schema(description = "메시지 ID", format = "uuid") UUID id,

    @Schema(description = "대화 ID", format = "uuid") UUID conversationId,

    @Schema(description = "메시지 생성 시간", format = "date-time") Instant createdAt,

    @Schema(description = "발신자 정보") UserSummary sender,

    @Schema(description = "수신자 정보") UserSummary receiver,

    @Schema(description = "메시지 내용") String content
) {
}
