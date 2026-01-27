package com.mopl.jpa.repository.content.sync.es;

import java.time.Instant;
import java.util.UUID;

public interface ContentEsSyncRow {

    UUID getId();

    String getType();

    String getTitle();

    String getDescription();

    String getThumbnailPath();

    int getReviewCount();

    double getAverageRating();

    double getPopularityScore();

    Instant getCreatedAt();

    Instant getUpdatedAt();
}
