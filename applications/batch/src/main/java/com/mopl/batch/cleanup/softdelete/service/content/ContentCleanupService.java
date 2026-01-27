package com.mopl.batch.cleanup.softdelete.service.content;

import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupPolicyResolver;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties;
import com.mopl.domain.repository.content.batch.ContentCleanupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentCleanupService {

    private static final int MAX_ITERATIONS = 10000;

    private final ContentCleanupTxService executor;
    private final ContentCleanupRepository contentCleanupRepository;
    private final SoftDeleteCleanupProperties props;
    private final SoftDeleteCleanupPolicyResolver policyResolver;

    public int cleanup() {
        int totalDeleted = 0;
        int iterations = 0;

        int chunkSize = policyResolver.chunkSize(props.content());
        long retentionDays = policyResolver.retentionDays(props.content());
        Instant threshold = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        while (iterations < MAX_ITERATIONS) {
            List<UUID> contentIds = contentCleanupRepository.findCleanupTargets(threshold, chunkSize);

            if (contentIds.isEmpty()) {
                break;
            }

            int deleted = executor.cleanupBatch(contentIds);
            if (deleted == 0) {
                log.warn("[SoftDeleteCleanup] content found {} targets but deleted 0, breaking to prevent infinite loop",
                    contentIds.size());
                break;
            }

            totalDeleted += deleted;
            iterations++;
        }

        if (iterations >= MAX_ITERATIONS) {
            log.warn("[SoftDeleteCleanup] content reached max iterations={}, totalDeleted={}",
                MAX_ITERATIONS, totalDeleted);
        }

        return totalDeleted;
    }
}
