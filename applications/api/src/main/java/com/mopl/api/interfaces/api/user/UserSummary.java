package com.mopl.api.interfaces.api.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record UserSummary(

    @Schema(description = "사용자 ID", format = "uuid") UUID userId,

    @Schema(description = "사용자 이름") String name,

    @Schema(description = "사용자 프로필 이미지 URL") String profileImageUrl
) {
}
