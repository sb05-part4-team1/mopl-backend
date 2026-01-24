package com.mopl.batch.cleanup.service.storage;

import com.mopl.batch.cleanup.properties.CleanupPolicyResolver;
import com.mopl.batch.cleanup.properties.CleanupProperties;
import com.mopl.domain.repository.content.ContentDeletionLogRepository;
import com.mopl.domain.repository.content.ContentDeletionLogItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageCleanupService {

    private final ContentDeletionLogRepository contentDeletionLogRepository;
    private final StorageCleanupTxService storageCleanupTxService;
    private final CleanupProperties cleanupProperties;
    private final CleanupPolicyResolver policyResolver;

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
