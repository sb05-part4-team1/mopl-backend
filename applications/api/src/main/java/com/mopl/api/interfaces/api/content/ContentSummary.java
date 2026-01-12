package com.mopl.api.interfaces.api.content;

import com.mopl.domain.model.content.ContentModel.ContentType;
import java.util.List;
import java.util.UUID;

public record ContentSummary(
    UUID id,
    ContentType type, // String -> ContentType
    String title,
    String description,
    String thumbnailUrl,
    List<String> tags,
    double averageRating,
    int reviewCount
) {
}
