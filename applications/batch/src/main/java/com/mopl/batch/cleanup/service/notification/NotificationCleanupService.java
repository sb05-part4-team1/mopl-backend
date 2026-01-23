package com.mopl.batch.cleanup.service.notification;

import com.mopl.batch.cleanup.properties.CleanupPolicyResolver;
import com.mopl.batch.cleanup.properties.CleanupProperties;
import com.mopl.domain.repository.notification.NotificationRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationCleanupService {

    private final NotificationRepository notificationRepository;
    private final NotificationCleanupTxService executor;
    private final CleanupProperties cleanupProperties;
    private final CleanupPolicyResolver policyResolver;

    public int cleanup() {
        int totalDeleted = 0;

        int chunkSize = policyResolver.chunkSize(cleanupProperties.getNotification());
        long retentionDays = policyResolver.retentionDaysRequired(cleanupProperties
            .getNotification());
        Instant threshold = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        while (true) {
            List<UUID> notificationIds = notificationRepository.findCleanupTargets(threshold,
                chunkSize);

            if (notificationIds.isEmpty()) {
                break;
            }

            totalDeleted += executor.cleanupBatch(notificationIds);
        }

        return totalDeleted;
    }
}
