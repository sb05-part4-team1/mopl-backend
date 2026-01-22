package com.mopl.batch.collect.tmdb.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbCollectScheduler {

    private final JobLauncher jobLauncher;
    private final Job tmdbCollectJob;
    private final Job tmdbGenreSyncJob;

    @Scheduled(cron = "0 20 2 * * *")
//    @Scheduled(fixedDelay = 300000) // 5분 테스트용
    public void runTmdbCollect() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(tmdbCollectJob, params);
        } catch (Exception e) {
            log.error("TMDB collect batch failed", e);
        }
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void runTmdbGenreSync() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(tmdbGenreSyncJob, params);
        } catch (Exception e) {
            log.error("TMDB genre sync batch failed", e);
        }
    }
}
