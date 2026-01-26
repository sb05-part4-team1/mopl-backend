package com.mopl.batch.sync.denormalized.service;

import com.mopl.jpa.repository.content.JpaContentRepository;
import com.mopl.jpa.repository.review.JpaReviewRepository;
import com.mopl.jpa.repository.review.projection.ReviewStatsProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentReviewStatsSyncTxService {

    private static final double EPSILON = 0.0001;

    private final JpaContentRepository jpaContentRepository;
    private final JpaReviewRepository jpaReviewRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean syncOne(UUID contentId) {
        return jpaContentRepository.findById(contentId)
            .map(content -> {
                int currentReviewCount = content.getReviewCount();
                double currentAverageRating = content.getAverageRating();

                ReviewStatsProjection stats = jpaReviewRepository.findReviewStatsByContentId(contentId);
                int actualReviewCount = stats != null ? stats.getReviewCount().intValue() : 0;
                double actualAverageRating = stats != null ? stats.getAverageRating() : 0.0;

                boolean reviewCountMismatch = currentReviewCount != actualReviewCount;
                boolean ratingMismatch = Math.abs(currentAverageRating - actualAverageRating) > EPSILON;

                if (reviewCountMismatch || ratingMismatch) {
                    jpaContentRepository.updateReviewStats(contentId, actualReviewCount, actualAverageRating);
                    log.info("[ContentReviewStatsSync] synced contentId={} reviewCount: {} -> {}, averageRating: {} -> {}",
                        contentId, currentReviewCount, actualReviewCount, currentAverageRating, actualAverageRating);
                    return true;
                }
                return false;
            })
            .orElse(false);
    }
}
