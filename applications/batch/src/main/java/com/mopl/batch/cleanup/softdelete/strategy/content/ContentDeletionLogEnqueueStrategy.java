package com.mopl.batch.cleanup.softdelete.strategy.content;

import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Profile("cleanup-deletion-log")
@RequiredArgsConstructor
public class ContentDeletionLogEnqueueStrategy implements ContentDeletionStrategy {

    private final ContentDeletionLogRepository contentDeletionLogRepository;

    @Override
    public int onDeleted(Map<UUID, String> thumbnailPathsByContentId) {
        int inserted = contentDeletionLogRepository.saveAll(thumbnailPathsByContentId);

        if (inserted != thumbnailPathsByContentId.size()) {
            LogContext.with("strategy", "deletionLogEnqueue")
                .and("requested", thumbnailPathsByContentId.size())
                .and("inserted", inserted)
                .warn("Deletion log save mismatch");
        }

        return inserted;
    }
}
