package com.mopl.batch.cleanup.softdelete.strategy.content;

import com.mopl.logging.context.LogContext;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Profile("cleanup-after-commit-delete")
@RequiredArgsConstructor
public class ContentAfterCommitThumbnailDeletionStrategy implements ContentDeletionStrategy {

    private final StorageProvider storageProvider;

    @Override
    public int onDeleted(Map<UUID, String> thumbnailPathsByContentId) {
        List<String> pathsToDelete = thumbnailPathsByContentId.values().stream()
            .filter(path -> path != null && !path.isBlank())
            .toList();

        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    for (String path : pathsToDelete) {
                        try {
                            storageProvider.delete(path);
                        } catch (Exception e) {
                            LogContext.with("strategy", "afterCommitThumbnailDeletion")
                                .and("path", path)
                                .warn("Thumbnail delete failed", e);
                        }
                    }
                }
            }
        );

        return pathsToDelete.size();
    }
}
