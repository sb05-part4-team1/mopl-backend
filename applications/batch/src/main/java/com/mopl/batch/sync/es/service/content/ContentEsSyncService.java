package com.mopl.batch.sync.es.service.content;

import com.mopl.batch.sync.es.config.EsSyncPolicyResolver;
import com.mopl.batch.sync.es.config.EsSyncProperties;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ContentEsSyncService {

    private final EsSyncProperties props;
    private final EsSyncPolicyResolver resolver;
    private final ContentEsSyncTxService txService;
    private final ContentSearchSyncPort contentSearchSyncPort;

    public int syncAll() {
        int totalUpserted = 0;

        Instant lastCreatedAt = null;
        UUID lastId = null;

        int chunkSize = resolver.chunkSize(props.content());

        while (true) {
            List<ContentModel> chunk = txService.fetchChunk(lastCreatedAt, lastId, chunkSize);

            if (chunk.isEmpty()) {
                break;
            }

            ContentModel last = chunk.getLast();
            lastCreatedAt = last.getCreatedAt();
            lastId = last.getId();

            contentSearchSyncPort.upsertAll(chunk);

            totalUpserted += chunk.size();
            LogContext.with("service", "contentEsSync")
                .and("upserted", totalUpserted)
                .and("lastCreatedAt", lastCreatedAt)
                .and("lastId", lastId)
                .debug("Sync progress");
        }

        return totalUpserted;
    }
}
