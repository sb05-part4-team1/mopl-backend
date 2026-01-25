package com.mopl.batch.sync.denormalized.service;

import com.mopl.batch.sync.denormalized.properties.DenormalizedSyncPolicyResolver;
import com.mopl.jpa.repository.review.JpaReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentReviewStatsSyncService {

    private final JpaReviewRepository jpaReviewRepository;
    private final ContentReviewStatsSyncTxService txService;
    private final DenormalizedSyncPolicyResolver policyResolver;

    public int sync() {
        Set<UUID> contentIds = jpaReviewRepository.findAllContentIds();

        if (contentIds.isEmpty()) {
            log.info("[ContentReviewStatsSync] no contents with reviews found");
            return 0;
        }

        int batchSize = policyResolver.resolve().batchSize();
        List<UUID> contentIdList = new ArrayList<>(contentIds);
        int totalSynced = 0;

        for (int i = 0; i < contentIdList.size(); i += batchSize) {
            List<UUID> batch = contentIdList.subList(i, Math.min(i + batchSize, contentIdList.size()));

            Map<UUID, ReviewStats> actualStats = jpaReviewRepository.findReviewStatsByContentIdIn(batch)
                .stream()
                .collect(Collectors.toMap(
                    row -> (UUID) row[0],
                    row -> new ReviewStats(((Long) row[1]).intValue(), (Double) row[2])
                ));

            int synced = txService.syncBatch(batch, actualStats);
            totalSynced += synced;

            log.debug("[ContentReviewStatsSync] batch processed offset={} batchSize={} synced={}",
                i, batch.size(), synced);
        }

        return totalSynced;
    }

    public record ReviewStats(int reviewCount, double averageRating) {
    }
}
