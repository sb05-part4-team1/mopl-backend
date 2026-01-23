package com.mopl.domain.model.review;

public record ReviewStats(
    double averageRating,
    int reviewCount
) {

    public static ReviewStats empty() {
        return new ReviewStats(0.0, 0);
    }
}
