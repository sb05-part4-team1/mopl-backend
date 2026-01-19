package com.mopl.batch.tmdb.scheduler;

import com.mopl.batch.tmdb.job.TmdbPopularContentJob;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmdbPopularContentScheduler {

    private final TmdbPopularContentJob job;

    @Scheduled(cron = "0 10 2 * * *")
    public void run() {
        job.run();
    }
}
