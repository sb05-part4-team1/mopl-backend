package com.mopl.search.content.index;

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

    @Override
    public void run(ApplicationArguments args) {
        IndexOperations indexOps = operations.indexOps(ContentDocument.class);
        String indexName = indexOps.getIndexCoordinates().getIndexName();

        log.info("Content index init start. index={}", indexName);

        try {
            boolean exists = indexOps.exists();
            log.debug("Content index exists check. index={}, exists={}", indexName, exists);

            if (exists) {
                boolean deleted = indexOps.delete();
                log.debug("Content index deleted. index={}, deleted={}", indexName, deleted);
            }

            boolean created = indexOps.create();
            log.debug("Content index created. index={}, created={}", indexName, created);

            boolean mappingApplied = indexOps.putMapping(indexOps.createMapping(ContentDocument.class));
            log.debug("Content index mapping applied. index={}, applied={}", indexName, mappingApplied);

            log.info("Content index init done. index={}", indexName);
        } catch (RuntimeException e) {
            log.error("Content index init failed. index={}", indexName, e);
            throw e;
        }
    }
}
