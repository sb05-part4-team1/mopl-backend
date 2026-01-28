package com.mopl.search.content.index;

import com.mopl.logging.context.LogContext;
import com.mopl.search.config.properties.SearchIndexProperties;
import com.mopl.search.document.ContentDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
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
                LogContext.with("index", indexName).info("Content index already exists - skip init");
                return;
            }

            if (exists) {
                boolean deleted = indexOps.delete();
                LogContext.with("index", indexName).and("deleted", deleted)
                    .warn("Content index recreate enabled");
            } else {
                LogContext.with("index", indexName).info("Content index not found - creating");
            }

            boolean created = indexOps.create();
            boolean mappingApplied = indexOps.putMapping(indexOps.createMapping(ContentDocument.class));

            LogContext.with("index", indexName)
                .and("created", created)
                .and("mappingApplied", mappingApplied)
                .info("Content index init completed");
        } catch (RuntimeException e) {
            LogContext.with("index", indexName).error("Content index init failed", e);
            throw e;
        }
    }
}
