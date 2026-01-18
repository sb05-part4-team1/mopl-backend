package com.mopl.batch.tsdb.job;

import com.mopl.batch.tsdb.service.TsdbLeagueSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TsdbLeagueSyncJob {

    private final TsdbLeagueSyncService service;

    public void run() {
        service.syncAll();
    }
}
