package com.mopl.batch.sync.es.initializer;

import com.mopl.batch.sync.es.service.content.ContentEsSyncService;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@ConditionalOnExpression(
    "${mopl.search.enabled:false} && ${mopl.batch.sync.es.initial-enabled:false}"
)
@RequiredArgsConstructor
public class ContentEsSyncInitializer implements ApplicationRunner {

    private final ContentEsSyncService syncService;

    @Override
    public void run(ApplicationArguments args) {
        LogContext.with("initializer", "contentEsSync").info("Initial sync started");
        int total = syncService.syncAll();
        LogContext.with("initializer", "contentEsSync")
            .and("totalUpserted", total)
            .info("Initial sync completed");
    }
}
