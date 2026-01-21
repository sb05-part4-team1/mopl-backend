package com.mopl.api.interfaces.api.watchingsession;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

import com.mopl.api.interfaces.api.content.ContentSummary;
import com.mopl.api.interfaces.api.user.UserSummary;

public record WatchingSessionDto(
    @Schema(description = "시청 세션 ID", format = "uuid") UUID id,

    @Schema(description = "시청 세션 생성 시간", format = "date-time") Instant createdAt,

    @Schema(description = "시청자 정보") UserSummary watcher,

    @Schema(description = "시청 중인 콘텐츠 정보") ContentSummary content
) {
}
