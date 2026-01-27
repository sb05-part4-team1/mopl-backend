package com.mopl.batch.sync.denormalized.service;

import com.mopl.domain.support.popularity.ContentPopularityPolicyPort;
import com.mopl.jpa.repository.content.JpaContentRepository;
import com.mopl.jpa.repository.review.JpaReviewRepository;
import com.mopl.jpa.repository.review.projection.ReviewStatsProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentReviewStatsSyncTxService {

    private static final double EPSILON = 0.0001;

    private final JpaContentRepository jpaContentRepository;
    private final JpaReviewRepository jpaReviewRepository;
    private final ContentPopularityPolicyPort popularityPolicy;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean syncOne(UUID contentId) {
        return jpaContentRepository.findById(contentId)
            .map(content -> {
                int currentReviewCount = content.getReviewCount();
                double currentAverageRating = content.getAverageRating();
                double currentPopularityScore = content.getPopularityScore();

                ReviewStatsProjection stats = jpaReviewRepository.findReviewStatsByContentId(contentId);
                int actualReviewCount = stats != null ? stats.getReviewCount().intValue() : 0;
                double actualAverageRating = stats != null ? stats.getAverageRating() : 0.0;
                double actualPopularityScore = calculatePopularityScore(actualReviewCount, actualAverageRating);

                boolean reviewCountMismatch = currentReviewCount != actualReviewCount;
                boolean ratingMismatch = Math.abs(currentAverageRating - actualAverageRating) > EPSILON;
                boolean popularityMismatch = Math.abs(currentPopularityScore - actualPopularityScore) > EPSILON;

                if (reviewCountMismatch || ratingMismatch || popularityMismatch) {
                    jpaContentRepository.updateReviewStats(contentId, actualReviewCount, actualAverageRating, actualPopularityScore);
                    log.info("[ContentReviewStatsSync] synced contentId={} reviewCount: {} -> {}, averageRating: {} -> {}, popularityScore: {} -> {}",
                        contentId, currentReviewCount, actualReviewCount, currentAverageRating, actualAverageRating, currentPopularityScore, actualPopularityScore);
                    return true;
                }
                return false;
            })
            .orElse(false);
    }

    private double calculatePopularityScore(int reviewCount, double averageRating) {
        double globalAverageRating = popularityPolicy.globalAverageRating();
        int minReviewCount = popularityPolicy.minimumReviewCount();
        double effectiveMinReviewCount = Math.max(minReviewCount, 1);

        return ((reviewCount / (reviewCount + effectiveMinReviewCount)) * averageRating)
               + ((effectiveMinReviewCount / (reviewCount + effectiveMinReviewCount)) * globalAverageRating);
    }
}
