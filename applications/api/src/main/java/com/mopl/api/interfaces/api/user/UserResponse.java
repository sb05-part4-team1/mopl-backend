package com.mopl.api.interfaces.api.user;

import com.mopl.domain.model.user.UserModel;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(

    @Schema(description = "사용자 ID", format = "uuid") UUID id,

    @Schema(description = "사용자 생성 시간", format = "date-time") Instant createdAt,

    @Schema(description = "이메일") String email,

    @Schema(description = "사용자 이름") String name,

    @Schema(description = "프로필 이미지 URL") String profileImageUrl,

    @Schema(description = "사용자 역할") UserModel.Role role,

    @Schema(description = "계정 잠금 여부") boolean locked
) {
}
