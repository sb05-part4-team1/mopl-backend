package com.mopl.batch.cleanup.service.storage;

import com.mopl.domain.repository.content.ContentDeletionLogRepository;
import com.mopl.domain.repository.content.dto.ContentDeletionLogItem;
import com.mopl.storage.provider.FileStorageProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageCleanupExecutor {

    private final FileStorageProvider fileStorageProvider;
    private final ContentDeletionLogRepository contentDeletionLogRepository;

    @Transactional
    public int cleanupBatch(List<ContentDeletionLogItem> targets) {
        List<UUID> successLogIds = new ArrayList<>();

        for (ContentDeletionLogItem target : targets) {
            String path = target.thumbnailPath();

            try {
                fileStorageProvider.delete(path);
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
            contentDeletionLogRepository.markImageProcessed(successLogIds);
        }

        return successLogIds.size();
    }
}
