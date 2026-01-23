package com.mopl.api.interfaces.api.content.dto;

import com.mopl.domain.model.content.ContentModel.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record ContentResponse(

    @Schema(description = "콘텐츠 ID") UUID id,
    @Schema(description = "콘텐츠 타입") ContentType type,
    @Schema(description = "콘텐츠 제목") String title,
    @Schema(description = "콘텐츠 설명") String description,
    @Schema(description = "썸네일 이미지 URL") String thumbnailUrl,
    @Schema(description = "콘텐츠 태그 목록") List<String> tags,
    @Schema(description = "평균 평점") double averageRating,
    @Schema(description = "리뷰 수") int reviewCount,
    @Schema(description = "시청자 수") long watcherCount
) {
}
