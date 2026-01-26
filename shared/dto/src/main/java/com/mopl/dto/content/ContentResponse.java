package com.mopl.dto.content;

import com.mopl.domain.model.content.ContentModel.ContentType;

import java.util.List;
import java.util.UUID;

public record ContentResponse(
    UUID id,
    ContentType type,
    String title,
    String description,
    String thumbnailUrl,
    List<String> tags,
    double averageRating,
    int reviewCount,
    long watcherCount
) {
}
