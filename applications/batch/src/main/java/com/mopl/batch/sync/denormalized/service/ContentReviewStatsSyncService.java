package com.mopl.batch.sync.denormalized.service;

import com.mopl.batch.sync.denormalized.config.DenormalizedSyncPolicyResolver;
import com.mopl.batch.sync.denormalized.config.DenormalizedSyncProperties;
import com.mopl.jpa.repository.denormalized.JpaDenormalizedSyncRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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
            log.debug("[ContentReviewStatsSync] processed chunk={} lastContentId={}", contentIds.size(), lastContentId);
        }

        if (iterations >= MAX_ITERATIONS) {
            log.warn("[ContentReviewStatsSync] reached max iterations={}, totalSynced={}", MAX_ITERATIONS, totalSynced);
        } else {
            log.info("[ContentReviewStatsSync] completed iterations={} synced={}", iterations, totalSynced);
        }

        return totalSynced;
    }
}
