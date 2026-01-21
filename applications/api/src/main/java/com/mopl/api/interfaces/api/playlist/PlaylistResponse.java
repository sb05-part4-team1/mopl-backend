package com.mopl.api.interfaces.api.playlist;

import com.mopl.api.interfaces.api.content.ContentSummary;
import com.mopl.api.interfaces.api.user.UserSummary;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaylistResponse(

    @Schema(description = "플레이리스트 ID", format = "uuid") UUID id,

    @Schema(description = "플레이리스트 소유쟈") UserSummary owner,

    @Schema(description = "플레이리스트 제목") String title,

    @Schema(description = "플레이리스트 설명") String description,

    @Schema(description = "수정된 시간", format = "date-time") Instant updatedAt,

    @Schema(description = "구독자 수", format = "int64") long subscriberCount,

    @Schema(description = "구독 여부") boolean subscribedByMe,

    @Schema(description = "플레이리스트에 포함된 콘텐츠 목록") List<ContentSummary> contents
) {
}
