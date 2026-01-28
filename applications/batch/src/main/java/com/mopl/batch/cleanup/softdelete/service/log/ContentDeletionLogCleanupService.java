package com.mopl.batch.cleanup.softdelete.service.log;

import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupPolicyResolver;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentDeletionLogCleanupService {

    private static final int MAX_ITERATIONS = 10000;

    private final ContentDeletionLogRepository contentDeletionLogRepository;
    private final ContentDeletionLogCleanupTxService executor;
    private final SoftDeleteCleanupProperties props;
    private final SoftDeleteCleanupPolicyResolver policyResolver;

    public int cleanup() {
        int totalDeleted = 0;
        int iterations = 0;

        int chunkSize = policyResolver.chunkSize(props.deletionLog());

        while (iterations < MAX_ITERATIONS) {
            List<UUID> logIds = contentDeletionLogRepository.findFullyProcessedLogIds(chunkSize);

            if (logIds.isEmpty()) {
                break;
            }

            int deleted = executor.cleanupBatch(logIds);
            if (deleted == 0) {
                LogContext.with("service", "softDeleteCleanup")
                    .and("entity", "deletionLog")
                    .and("foundTargets", logIds.size())
                    .warn("Found targets but deleted 0, breaking to prevent infinite loop");
                break;
            }

            totalDeleted += deleted;
            iterations++;
        }

        if (iterations >= MAX_ITERATIONS) {
            LogContext.with("service", "softDeleteCleanup")
                .and("entity", "deletionLog")
                .and("maxIterations", MAX_ITERATIONS)
                .and("totalDeleted", totalDeleted)
                .warn("Reached max iterations");
        }

        return totalDeleted;
    }
}
