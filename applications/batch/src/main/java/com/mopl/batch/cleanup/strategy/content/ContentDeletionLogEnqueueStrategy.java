package com.mopl.batch.cleanup.strategy.content;

import com.mopl.domain.repository.content.ContentDeletionLogRepository;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("cleanup-deletion-log")
@RequiredArgsConstructor
public class ContentDeletionLogEnqueueStrategy implements ContentDeletionStrategy {

    private final ContentDeletionLogRepository contentDeletionLogRepository;

    @Override
    public int onDeleted(Map<UUID, String> thumbnailPathsByContentId) {
        int inserted = contentDeletionLogRepository.saveAll(thumbnailPathsByContentId);

        if (inserted != thumbnailPathsByContentId.size()) {
            log.warn(
                "content deletion log save mismatch. requested={} inserted={}",
                thumbnailPathsByContentId.size(),
                inserted
            );
        }

        return inserted;
    }
}
