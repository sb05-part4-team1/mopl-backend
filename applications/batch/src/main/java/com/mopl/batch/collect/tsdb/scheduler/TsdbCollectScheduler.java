package com.mopl.batch.collect.tsdb.scheduler;

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
public class TsdbCollectScheduler {

    private final JobLauncher jobLauncher;
    private final Job tsdbLeagueEventCollectJob;
    private final Job tsdbLeagueSyncJob;

    @Scheduled(cron = "0 10 2 * * *")
    public void runTsdbCollect() {
        LogContext.with("job", "tsdbCollect").info("Scheduled job started");
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(tsdbLeagueEventCollectJob, params);
        } catch (Exception e) {
            LogContext.with("job", "tsdbCollect").error("Batch failed", e);
        }
    }

    @Scheduled(cron = "0 30 3 * * MON")
    public void runTsdbLeagueSync() {
        LogContext.with("job", "tsdbLeagueSync").info("Scheduled job started");
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(tsdbLeagueSyncJob, params);
        } catch (Exception e) {
            LogContext.with("job", "tsdbLeagueSync").error("Batch failed", e);
        }
    }
}
