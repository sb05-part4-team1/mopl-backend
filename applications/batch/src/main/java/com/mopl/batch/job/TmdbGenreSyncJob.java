package com.mopl.batch.job;

import com.mopl.batch.service.TmdbGenreSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmdbGenreSyncJob {

    private final TmdbGenreSyncService syncService;

    public void run() {
        syncService.syncAll();
    }
}
