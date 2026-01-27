package com.mopl.batch.cleanup.retention.service.log;

import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentDeletionLogCleanupTxService {

    private final ContentDeletionLogRepository contentDeletionLogRepository;

    @Transactional
    public int cleanupBatch(List<UUID> logIds) {
        return contentDeletionLogRepository.deleteByIdIn(logIds);
    }
}
