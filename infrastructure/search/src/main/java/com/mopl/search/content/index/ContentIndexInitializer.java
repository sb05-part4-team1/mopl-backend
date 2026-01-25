package com.mopl.search.content.index;

import com.mopl.search.config.index.SearchIndexProperties;
import com.mopl.search.document.ContentDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
public class ContentIndexInitializer implements ApplicationRunner {

    private final ElasticsearchOperations operations;
    private final SearchIndexProperties indexProps;

    @Override
    public void run(ApplicationArguments args) {
        IndexOperations indexOps = operations.indexOps(ContentDocument.class);
        String indexName = indexOps.getIndexCoordinates().getIndexName();

        try {
            boolean exists = indexOps.exists();

            if (exists && !indexProps.isRecreateOnStartup()) {
                log.info("Content index already exists. skip init. index={}", indexName);
                return;
            }

            if (exists) {
                boolean deleted = indexOps.delete();
                log.warn("Content index recreate enabled. index={}, deleted={}", indexName, deleted);
            } else {
                log.info("Content index not found. create index. index={}", indexName);
            }

            boolean created = indexOps.create();
            boolean mappingApplied = indexOps.putMapping(indexOps.createMapping(ContentDocument.class));

            log.info(
                "Content index init done. index={}, created={}, mappingApplied={}",
                indexName,
                created,
                mappingApplied
            );
        } catch (RuntimeException e) {
            log.error("Content index init failed. index={}", indexName, e);
            throw e;
        }
    }
}
