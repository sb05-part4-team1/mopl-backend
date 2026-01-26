package com.mopl.jpa.repository.review.projection;

import java.util.UUID;

public interface ReviewStatsProjection {

    UUID getContentId();

    Long getReviewCount();

    Double getAverageRating();
}
