package com.mopl.batch.cleanup.service.notification;

import com.mopl.domain.repository.notification.NotificationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationCleanupTxService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public int cleanupBatch(List<UUID> notificationIds) {
        return notificationRepository.deleteByIdIn(notificationIds);
    }
}
