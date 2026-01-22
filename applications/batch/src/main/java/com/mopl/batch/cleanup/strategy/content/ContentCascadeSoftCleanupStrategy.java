package com.mopl.batch.cleanup.strategy.content;

import com.mopl.storage.provider.FileStorageProvider;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@Profile("cleanup-a")
@RequiredArgsConstructor
public class ContentCascadeSoftCleanupStrategy implements ContentDeletionStrategy {

    private final FileStorageProvider fileStorageProvider;

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
                            fileStorageProvider.delete(path);
                        } catch (Exception e) {
                            log.warn("thumbnail delete failed. path={}", path, e);
                        }
                    }
                }
            }
        );

        return pathsToDelete.size();
    }
}
