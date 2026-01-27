package com.mopl.batch.collect.tmdb.scheduler;

import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmdbCollectScheduler {

    private final JobLauncher jobLauncher;
    private final Job tmdbCollectJob;
    private final Job tmdbGenreSyncJob;

    @Scheduled(cron = "0 20 2 * * *")
    public void runTmdbCollect() {
        LogContext.with("job", "tmdbCollect").info("Scheduled job started");
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(tmdbCollectJob, params);
        } catch (Exception e) {
            LogContext.with("job", "tmdbCollect").error("Batch failed", e);
        }
    }

    @Scheduled(cron = "0 30 3 * * MON")
    public void runTmdbGenreSync() {
        LogContext.with("job", "tmdbGenreSync").info("Scheduled job started");
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(tmdbGenreSyncJob, params);
        } catch (Exception e) {
            LogContext.with("job", "tmdbGenreSync").error("Batch failed", e);
        }
    }
}
