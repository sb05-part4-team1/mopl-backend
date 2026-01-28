package com.mopl.batch.sync.es.initializer;

import com.mopl.batch.sync.es.service.content.ContentEsSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ContentEsSyncInitializer implements ApplicationRunner {

    private final ContentEsSyncService syncService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("ES initial sync start");
        int total = syncService.syncAll();
        log.info("ES initial sync done. totalUpserted={}", total);
    }
}
