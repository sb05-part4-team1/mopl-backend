package com.mopl.domain.support.popularity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PopularityScoreCalculator {

    public static double calculate(
        int reviewCount,
        double averageRating,
        double globalAverageRating,
        int minimumReviewCount
    ) {
        double effectiveMinReviewCount = Math.max(minimumReviewCount, 1);

        return (((double) reviewCount / ((double) reviewCount + effectiveMinReviewCount)) * averageRating)
               + ((effectiveMinReviewCount / ((double) reviewCount + effectiveMinReviewCount)) * globalAverageRating);
    }
}
