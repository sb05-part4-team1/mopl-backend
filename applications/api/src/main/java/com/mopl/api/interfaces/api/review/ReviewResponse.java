package com.mopl.api.interfaces.api.review;

import com.mopl.api.interfaces.api.user.UserSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record ReviewResponse(

    @Schema(description = "리뷰 ID", format = "uuid") UUID id,

    @Schema(description = "콘텐츠 ID", format = "uuid") UUID contentId,

    @Schema(description = "작성자 정보") UserSummary author,

    @Schema(description = "리뷰 내용") String text,

    @Schema(description = "평점(0.0~5.0)", format = "double") double rating
) {
}
