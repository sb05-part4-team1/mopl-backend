package com.mopl.batch.scheduler;

import com.mopl.batch.job.TmdbGenreSyncJob;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmdbGenreSyncScheduler {

    private final TmdbGenreSyncJob genreSyncJob;

    @Scheduled(cron = "0 0 3 * * MON")
    public void runGenreSync() {
        genreSyncJob.run();
    }
}
