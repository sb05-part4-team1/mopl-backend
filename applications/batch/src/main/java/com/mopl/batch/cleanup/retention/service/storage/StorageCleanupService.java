package com.mopl.batch.cleanup.retention.service.storage;

import com.mopl.batch.cleanup.retention.config.RetentionCleanupPolicyResolver;
import com.mopl.batch.cleanup.retention.config.RetentionCleanupProperties;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import com.mopl.domain.repository.content.batch.ContentDeletionLogItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageCleanupService {

    private final ContentDeletionLogRepository contentDeletionLogRepository;
    private final StorageCleanupTxService storageCleanupTxService;
    private final RetentionCleanupProperties cleanupProperties;
    private final RetentionCleanupPolicyResolver policyResolver;

    public int cleanup() {
        int totalDeletedFiles = 0;

        int chunkSize = policyResolver.chunkSize(cleanupProperties.getStorage());

        while (true) {
            List<ContentDeletionLogItem> targets = contentDeletionLogRepository
                .findImageCleanupTargets(chunkSize);

            if (targets.isEmpty()) {
                break;
            }

            totalDeletedFiles += storageCleanupTxService.cleanupBatch(targets);
        }

        return totalDeletedFiles;
    }
}
