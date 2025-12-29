package com.mopl.api.interfaces.api.content;

import java.util.List;
import java.util.UUID;

/**
 * 콘텐츠 조회 응답 DTO
 */
public record ContentResponse(
    UUID id,
    String type,
    String title,
    String description,
    String thumbnailUrl,
    List<String> tags,
    double averageRating,
    int reviewCount,
    long watcherCount
) {
}
