package com.mopl.batch.cleanup.softdelete.service.content;

import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupPolicyResolver;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties;
import com.mopl.domain.repository.content.batch.ContentCleanupRepository;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
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
                LogContext.with("service", "softDeleteCleanup")
                    .and("entity", "content")
                    .and("foundTargets", contentIds.size())
                    .warn("Found targets but deleted 0, breaking to prevent infinite loop");
                break;
            }

            totalDeleted += deleted;
            iterations++;
        }

        if (iterations >= MAX_ITERATIONS) {
            LogContext.with("service", "softDeleteCleanup")
                .and("entity", "content")
                .and("maxIterations", MAX_ITERATIONS)
                .and("totalDeleted", totalDeleted)
                .warn("Reached max iterations");
        }

        return totalDeleted;
    }
}
