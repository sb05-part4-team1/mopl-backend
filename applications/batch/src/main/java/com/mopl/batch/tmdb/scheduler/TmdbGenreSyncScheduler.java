package com.mopl.batch.tmdb.scheduler;

import com.mopl.batch.tmdb.job.TmdbGenreSyncJob;
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
