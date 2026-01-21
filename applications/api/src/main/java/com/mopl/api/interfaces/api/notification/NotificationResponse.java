package com.mopl.api.interfaces.api.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

import com.mopl.domain.model.notification.NotificationLevel;

public record NotificationResponse(

    @Schema(description = "알림 ID", format = "uuid") UUID id,

    @Schema(description = "알림 생성 시간", format = "date-time") Instant createdAt,

    @Schema(description = "수신자 ID", format = "uuid") UUID receiverId,

    @Schema(description = "알림 제목") String title,

    @Schema(description = "알림 내용") String content,

    @Schema(description = "알림 수준") NotificationLevel level
) {
}
