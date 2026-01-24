package com.mopl.batch.cleanup.service.content;

import com.mopl.batch.cleanup.properties.CleanupPolicyResolver;
import com.mopl.batch.cleanup.properties.CleanupProperties;
import com.mopl.domain.repository.content.ContentCleanupRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentCleanupService {

    private final ContentCleanupTxService executor;
    private final ContentCleanupRepository contentCleanupRepository;
    private final CleanupProperties cleanupProperties;
    private final CleanupPolicyResolver policyResolver;

    public int cleanup() {
        int totalDeleted = 0;

        int chunkSize = policyResolver.chunkSize(cleanupProperties.getContent());
        long retentionDays = policyResolver.retentionDaysRequired(cleanupProperties.getContent());
        Instant threshold = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        while (true) {
            List<UUID> contentIds = contentCleanupRepository.findCleanupTargets(threshold, chunkSize);

            if (contentIds.isEmpty()) {
                break;
            }

            totalDeleted += executor.cleanupBatch(contentIds);
        }

        return totalDeleted;
    }
}
