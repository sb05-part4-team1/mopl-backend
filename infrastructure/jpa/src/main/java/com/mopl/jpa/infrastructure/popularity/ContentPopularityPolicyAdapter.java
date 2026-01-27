package com.mopl.jpa.infrastructure.popularity;

import com.mopl.domain.repository.setting.SystemConfigRepository;
import com.mopl.domain.support.popularity.ContentPopularityPolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentPopularityPolicyAdapter implements ContentPopularityPolicyPort {

    private static final double DEFAULT_GLOBAL_AVG_RATING = 3.7;
    private static final int DEFAULT_MIN_REVIEW_COUNT = 10;

    private final SystemConfigRepository repo;

    @Override
    public double globalAverageRating() {
        String value = repo.findValue("CONTENT_GLOBAL_AVG_RATING")
            .orElse(Double.toString(DEFAULT_GLOBAL_AVG_RATING));

        return parseDoubleOrDefault(value);
    }

    @Override
    public int minimumReviewCount() {
        String value = repo.findValue("CONTENT_POPULARITY_M")
            .orElse(Integer.toString(DEFAULT_MIN_REVIEW_COUNT));

        return parseIntOrDefault(value);
    }

    private double parseDoubleOrDefault(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return DEFAULT_GLOBAL_AVG_RATING;
        }
    }

    private int parseIntOrDefault(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return DEFAULT_MIN_REVIEW_COUNT;
        }
    }
}
