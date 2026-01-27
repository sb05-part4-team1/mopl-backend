package com.mopl.batch.cleanup.retention.service.log;

import com.mopl.batch.cleanup.retention.config.RetentionCleanupPolicyResolver;
import com.mopl.batch.cleanup.retention.config.RetentionCleanupProperties;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentDeletionLogCleanupService {

    private final ContentDeletionLogRepository contentDeletionLogRepository;
    private final ContentDeletionLogCleanupTxService executor;
    private final RetentionCleanupProperties cleanupProperties;
    private final RetentionCleanupPolicyResolver policyResolver;

    public int cleanup() {
        int totalDeleted = 0;

        int chunkSize = policyResolver.chunkSize(cleanupProperties.getDeletionLog());

        while (true) {
            List<UUID> logIds = contentDeletionLogRepository.findFullyProcessedLogIds(chunkSize);

            if (logIds.isEmpty()) {
                break;
            }

            totalDeleted += executor.cleanupBatch(logIds);
        }

        return totalDeleted;
    }
}
