package com.mopl.batch.cleanup.softdelete.service.storage;

import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupPolicyResolver;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties;
import com.mopl.domain.repository.content.batch.ContentDeletionLogItem;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
                log.warn("[SoftDeleteCleanup] storage found {} targets but deleted 0, breaking to prevent infinite loop",
                    targets.size());
                break;
            }

            totalDeletedFiles += deleted;
            iterations++;
        }

        if (iterations >= MAX_ITERATIONS) {
            log.warn("[SoftDeleteCleanup] storage reached max iterations={}, totalDeletedFiles={}",
                MAX_ITERATIONS, totalDeletedFiles);
        }

        return totalDeletedFiles;
    }
}
