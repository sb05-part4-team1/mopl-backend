package com.mopl.batch.sync.denormalized.service;

import com.mopl.domain.support.popularity.ContentPopularityPolicyPort;
import com.mopl.jpa.repository.content.JpaContentRepository;
import com.mopl.jpa.repository.denormalized.JpaDenormalizedSyncRepository;
import com.mopl.jpa.repository.denormalized.projection.ReviewStatsProjection;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentReviewStatsSyncTxService {

    private static final double EPSILON = 0.0001;

    private final JpaContentRepository jpaContentRepository;
    private final JpaDenormalizedSyncRepository denormalizedSyncRepository;
    private final ContentPopularityPolicyPort popularityPolicy;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean syncOne(UUID contentId) {
        return jpaContentRepository.findById(contentId)
            .map(content -> {
                int currentReviewCount = content.getReviewCount();
                double currentAverageRating = content.getAverageRating();
                double currentPopularityScore = content.getPopularityScore();

                ReviewStatsProjection stats = denormalizedSyncRepository.findReviewStatsByContentId(contentId);
                int actualReviewCount = stats != null ? stats.getReviewCount().intValue() : 0;
                double actualAverageRating = stats != null ? stats.getAverageRating() : 0.0;
                double actualPopularityScore = calculatePopularityScore(actualReviewCount, actualAverageRating);

                boolean reviewCountMismatch = currentReviewCount != actualReviewCount;
                boolean ratingMismatch = Math.abs(currentAverageRating - actualAverageRating) > EPSILON;
                boolean popularityMismatch = Math.abs(currentPopularityScore - actualPopularityScore) > EPSILON;

                if (reviewCountMismatch || ratingMismatch || popularityMismatch) {
                    denormalizedSyncRepository.updateReviewStats(contentId, actualReviewCount, actualAverageRating, actualPopularityScore);
                    LogContext.with("service", "contentReviewStatsSyncTx")
                        .and("contentId", contentId)
                        .and("reviewCountBefore", currentReviewCount)
                        .and("reviewCountAfter", actualReviewCount)
                        .and("averageRatingBefore", currentAverageRating)
                        .and("averageRatingAfter", actualAverageRating)
                        .and("popularityScoreBefore", currentPopularityScore)
                        .and("popularityScoreAfter", actualPopularityScore)
                        .debug("Stats synced");
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
