package com.mopl.batch.sync.es.service.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.sync.es.ContentEsSyncRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
@Service
public class ContentEsSyncTxService {

    private final ContentEsSyncRepository repository;

    @Transactional(readOnly = true)
    public List<ContentModel> fetchChunk(Instant lastCreatedAt, UUID lastId, int limit) {
        String lastIdStr = (lastId == null) ? null : lastId.toString();
        return repository.findSyncTargets(lastCreatedAt, lastIdStr, limit);
    }
}
