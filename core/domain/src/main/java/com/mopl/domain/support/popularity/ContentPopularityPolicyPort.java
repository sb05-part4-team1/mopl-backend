package com.mopl.domain.support.popularity;

public interface ContentPopularityPolicyPort {

    double globalAverageRating();

    int minimumReviewCount();

    default double calculatePopularityScore(int reviewCount, double averageRating) {
        return PopularityScoreCalculator.calculate(
            reviewCount,
            averageRating,
            globalAverageRating(),
            minimumReviewCount()
        );
    }
}
