package com.mopl.api.interfaces.api.content;

import java.util.List;
import java.util.UUID;

public record ContentSummary(
    UUID id,
    String type,
    String title,
    String description,
    String thumbnailUrl,
    List<String> tags,
    double averageRating,
    int reviewCount
) {
}
