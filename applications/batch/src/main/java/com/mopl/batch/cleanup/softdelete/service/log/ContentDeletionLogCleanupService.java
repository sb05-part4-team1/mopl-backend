package com.mopl.batch.cleanup.softdelete.service.log;

import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupPolicyResolver;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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
                log.warn("[SoftDeleteCleanup] deletionLog found {} targets but deleted 0, breaking to prevent infinite loop",
                    logIds.size());
                break;
            }

            totalDeleted += deleted;
            iterations++;
        }

        if (iterations >= MAX_ITERATIONS) {
            log.warn("[SoftDeleteCleanup] deletionLog reached max iterations={}, totalDeleted={}",
                MAX_ITERATIONS, totalDeleted);
        }

        return totalDeleted;
    }
}
