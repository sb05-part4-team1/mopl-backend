package com.mopl.batch.cleanup.softdelete.service.storage;

import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupPolicyResolver;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties;
import com.mopl.domain.repository.content.batch.ContentDeletionLogItem;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageCleanupService {

    private static final int MAX_ITERATIONS = 10000;

    private final ContentDeletionLogRepository contentDeletionLogRepository;
    private final StorageCleanupTxService storageCleanupTxService;
    private final SoftDeleteCleanupProperties props;
    private final SoftDeleteCleanupPolicyResolver policyResolver;

    public int cleanup() {
        int totalDeletedFiles = 0;
        int iterations = 0;

        int chunkSize = policyResolver.chunkSize(props.storage());

        while (iterations < MAX_ITERATIONS) {
            List<ContentDeletionLogItem> targets = contentDeletionLogRepository
                .findImageCleanupTargets(chunkSize);

            if (targets.isEmpty()) {
                break;
            }

            int deleted = storageCleanupTxService.cleanupBatch(targets);
            if (deleted == 0) {
                LogContext.with("service", "softDeleteCleanup")
                    .and("entity", "storage")
                    .and("foundTargets", targets.size())
                    .warn("Found targets but deleted 0, breaking to prevent infinite loop");
                break;
            }

            totalDeletedFiles += deleted;
            iterations++;
        }

        if (iterations >= MAX_ITERATIONS) {
            LogContext.with("service", "softDeleteCleanup")
                .and("entity", "storage")
                .and("maxIterations", MAX_ITERATIONS)
                .and("totalDeletedFiles", totalDeletedFiles)
                .warn("Reached max iterations");
        }

        return totalDeletedFiles;
    }
}
