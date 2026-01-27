package com.mopl.batch.sync.denormalized.service;

import com.mopl.jpa.repository.review.JpaReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentReviewStatsSyncService {

    private final JpaReviewRepository jpaReviewRepository;
    private final ContentReviewStatsSyncTxService txService;

    public int sync() {
        Set<UUID> contentIds = jpaReviewRepository.findAllContentIds();

        if (contentIds.isEmpty()) {
            log.info("[ContentReviewStatsSync] no contents with reviews found");
            return 0;
        }

        int totalSynced = 0;
        for (UUID contentId : contentIds) {
            if (txService.syncOne(contentId)) {
                totalSynced++;
            }
        }

        log.info("[ContentReviewStatsSync] completed total={} synced={}", contentIds.size(), totalSynced);
        return totalSynced;
    }
}
