package com.mopl.batch.sync.denormalized.service;

import com.mopl.batch.sync.denormalized.service.ContentReviewStatsSyncService.ReviewStats;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.repository.content.JpaContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentReviewStatsSyncTxService {

    private static final double EPSILON = 0.0001;

    private final JpaContentRepository jpaContentRepository;

    @Transactional
    public int syncBatch(List<UUID> contentIds, Map<UUID, ReviewStats> actualStats) {
        List<ContentEntity> contents = jpaContentRepository.findAllById(contentIds);
        int synced = 0;

        for (ContentEntity content : contents) {
            ReviewStats stats = actualStats.getOrDefault(
                content.getId(),
                new ReviewStats(0, 0.0)
            );

            boolean reviewCountMismatch = content.getReviewCount() != stats.reviewCount();
            boolean ratingMismatch = Math.abs(content.getAverageRating() - stats.averageRating()) > EPSILON;

            if (reviewCountMismatch || ratingMismatch) {
                jpaContentRepository.updateReviewStats(
                    content.getId(),
                    stats.reviewCount(),
                    stats.averageRating()
                );
                log.info("[ContentReviewStatsSync] synced contentId={} reviewCount: {} -> {}, averageRating: {} -> {}",
                    content.getId(),
                    content.getReviewCount(), stats.reviewCount(),
                    content.getAverageRating(), stats.averageRating());
                synced++;
            }
        }

        return synced;
    }
}
