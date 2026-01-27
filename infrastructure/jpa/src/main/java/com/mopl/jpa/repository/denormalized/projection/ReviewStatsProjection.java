package com.mopl.jpa.repository.denormalized.projection;

import java.util.UUID;

public interface ReviewStatsProjection {

    UUID getContentId();

    Long getReviewCount();

    Double getAverageRating();
}
