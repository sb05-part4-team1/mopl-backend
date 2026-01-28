package com.mopl.batch.cleanup.softdelete.service.storage;

import com.mopl.domain.repository.content.batch.ContentDeletionLogItem;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import com.mopl.logging.context.LogContext;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageCleanupTxService {

    private final StorageProvider storageProvider;
    private final ContentDeletionLogRepository contentDeletionLogRepository;

    @Transactional
    public int cleanupBatch(List<ContentDeletionLogItem> targets) {
        Instant now = Instant.now();

        List<UUID> successLogIds = new ArrayList<>();

        for (ContentDeletionLogItem target : targets) {
            String path = target.thumbnailPath();

            try {
                storageProvider.delete(path);
                successLogIds.add(target.logId());
            } catch (Exception e) {
                LogContext.with("service", "storageCleanupTx")
                    .and("logId", target.logId())
                    .and("contentId", target.contentId())
                    .and("path", path)
                    .warn("Thumbnail delete failed", e);
            }
        }

        if (!successLogIds.isEmpty()) {
            contentDeletionLogRepository.markImageProcessed(successLogIds, now);
        }

        return successLogIds.size();
    }
}
