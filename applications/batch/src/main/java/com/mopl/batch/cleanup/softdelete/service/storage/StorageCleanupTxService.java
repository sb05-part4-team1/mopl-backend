package com.mopl.batch.cleanup.softdelete.service.storage;

import com.mopl.domain.repository.content.batch.ContentDeletionLogItem;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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
                log.warn(
                    "thumbnail delete failed. logId={}, contentId={}, path={}",
                    target.logId(),
                    target.contentId(),
                    path,
                    e
                );
            }
        }

        if (!successLogIds.isEmpty()) {
            contentDeletionLogRepository.markImageProcessed(successLogIds, now);
        }

        return successLogIds.size();
    }
}
