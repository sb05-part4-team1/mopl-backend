package com.mopl.batch.collect.tsdb.scheduler;

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
public class TsdbCollectScheduler {

    private final JobLauncher jobLauncher;
    private final Job tsdbLeagueEventCollectJob;
    private final Job tsdbLeagueSyncJob;

    @Scheduled(cron = "0 10 2 * * *")
    public void runTsdbCollect() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(tsdbLeagueEventCollectJob, params);
        } catch (Exception e) {
            log.error("TSDB collect batch failed", e);
        }
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void runTsdbLeagueSync() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(tsdbLeagueSyncJob, params);
        } catch (Exception e) {
            log.error("TSDB league sync batch failed", e);
        }
    }
}
