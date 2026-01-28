package com.mopl.batch.sync.denormalized.service;

import com.mopl.batch.sync.denormalized.config.DenormalizedSyncPolicyResolver;
import com.mopl.batch.sync.denormalized.config.DenormalizedSyncProperties;
import com.mopl.jpa.repository.denormalized.JpaDenormalizedSyncRepository;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentReviewStatsSyncService {

    private static final UUID MIN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final int MAX_ITERATIONS = 10000;

    private final JpaDenormalizedSyncRepository denormalizedSyncRepository;
    private final ContentReviewStatsSyncTxService txService;
    private final DenormalizedSyncProperties props;
    private final DenormalizedSyncPolicyResolver policyResolver;

    public int sync() {
        int chunkSize = policyResolver.chunkSize(props.contentReviewStats());
        int totalSynced = 0;
        int iterations = 0;
        UUID lastContentId = MIN_UUID;

        while (iterations < MAX_ITERATIONS) {
            List<UUID> contentIds = denormalizedSyncRepository.findContentIdsAfter(lastContentId, chunkSize);

            if (contentIds.isEmpty()) {
                break;
            }

            for (UUID contentId : contentIds) {
                if (txService.syncOne(contentId)) {
                    totalSynced++;
                }
            }

            lastContentId = contentIds.getLast();
            iterations++;
            LogContext.with("service", "contentReviewStatsSync")
                .and("chunkSize", contentIds.size())
                .and("lastContentId", lastContentId)
                .debug("Chunk processed");
        }

        if (iterations >= MAX_ITERATIONS) {
            LogContext.with("service", "contentReviewStatsSync")
                .and("maxIterations", MAX_ITERATIONS)
                .and("totalSynced", totalSynced)
                .warn("Reached max iterations");
        } else {
            LogContext.with("service", "contentReviewStatsSync")
                .and("iterations", iterations)
                .and("synced", totalSynced)
                .info("Sync completed");
        }

        return totalSynced;
    }
}
