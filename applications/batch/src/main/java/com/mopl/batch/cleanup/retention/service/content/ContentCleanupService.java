package com.mopl.batch.cleanup.retention.service.content;

import com.mopl.batch.cleanup.retention.config.RetentionCleanupPolicyResolver;
import com.mopl.batch.cleanup.retention.config.RetentionCleanupProperties;
import com.mopl.domain.repository.content.batch.ContentCleanupRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentCleanupService {

    private static final int MAX_ITERATIONS = 10000;

    private final ContentCleanupTxService executor;
    private final ContentCleanupRepository contentCleanupRepository;
    private final RetentionCleanupProperties props;
    private final RetentionCleanupPolicyResolver policyResolver;

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
                log.warn("[RetentionCleanup] content found {} targets but deleted 0, breaking to prevent infinite loop",
                    contentIds.size());
                break;
            }

            totalDeleted += deleted;
            iterations++;
        }

        if (iterations >= MAX_ITERATIONS) {
            log.warn("[RetentionCleanup] content reached max iterations={}, totalDeleted={}",
                MAX_ITERATIONS, totalDeleted);
        }

        return totalDeleted;
    }
}
