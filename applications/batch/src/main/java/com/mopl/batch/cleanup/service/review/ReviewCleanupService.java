package com.mopl.batch.cleanup.service.review;

import com.mopl.batch.cleanup.properties.CleanupPolicyResolver;
import com.mopl.batch.cleanup.properties.CleanupProperties;
import com.mopl.domain.repository.review.ReviewRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewCleanupService {

    private final ReviewRepository reviewRepository;
    private final ReviewCleanupExecutor executor;
    private final CleanupProperties cleanupProperties;
    private final CleanupPolicyResolver policyResolver;

    public int cleanup() {
        int totalDeleted = 0;

        int chunkSize = policyResolver.chunkSize(cleanupProperties.getReview());
        long retentionDays = policyResolver.retentionDaysRequired(cleanupProperties.getReview());
        Instant threshold = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        while (true) {
            List<UUID> reviewIds = reviewRepository.findCleanupTargets(threshold, chunkSize);

            if (reviewIds.isEmpty()) {
                break;
            }

            totalDeleted += executor.cleanupBatch(reviewIds);
        }

        return totalDeleted;
    }
}
