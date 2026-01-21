package com.mopl.api.interfaces.api.content;

import com.mopl.domain.model.content.ContentModel.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

public record ContentSummary(

    @Schema(description = "콘텐츠 ID", format = "uuid") UUID id,

    @Schema(description = "콘텐츠 타입") ContentType type, // String -> ContentType

    @Schema(description = "콘텐츠 제목") String title,

    @Schema(description = "콘텐츠 설명") String description,

    @Schema(description = "썸네일 이미지 URL") String thumbnailUrl,

    @Schema(description = "콘텐츠 태그 목록") List<String> tags,

    @Schema(description = "평균 평점", format = "double") double averageRating,

    @Schema(description = "리뷰 개수", format = "int32") int reviewCount
) {
}
