package com.mopl.api.interfaces.api.watchingsession.dev;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * local 환경에서만 사용하는 시청 세션 테스트 데이터 주입 요청 DTO. (Websocket 연결전 테스트용)
 */
public record DevWatchingSessionUpsertRequest(

    @NotNull UUID watcherId,
    @NotBlank String watcherName,

    UUID contentId,
    @NotBlank String contentTitle

) {
}
