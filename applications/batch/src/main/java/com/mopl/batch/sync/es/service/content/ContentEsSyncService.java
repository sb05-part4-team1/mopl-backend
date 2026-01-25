package com.mopl.batch.sync.es.service.content;

import com.mopl.batch.sync.es.properties.EsSyncPolicyResolver;
import com.mopl.batch.sync.es.properties.EsSyncProperties;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
public class ContentEsSyncService {

    private final EsSyncProperties props;
    private final EsSyncPolicyResolver resolver;
    private final ContentEsSyncTxService txService;
    private final ContentSearchSyncPort contentSearchSyncPort;

    public int syncAll() {
        int totalUpserted = 0;

        Instant lastCreatedAt = null;
        UUID lastId = null;

        int chunkSize = resolver.chunkSize(props.getContent());

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
            log.info("ES sync progress: upserted={}, lastCreatedAt={}, lastId={}", totalUpserted, lastCreatedAt, lastId);
        }

        return totalUpserted;
    }
}
