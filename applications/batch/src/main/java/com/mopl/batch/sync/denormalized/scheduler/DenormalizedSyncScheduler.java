package com.mopl.batch.sync.denormalized.scheduler;

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
public class DenormalizedSyncScheduler {

    private final JobLauncher jobLauncher;
    private final Job denormalizedSyncJob;

    @Scheduled(cron = "0 0 3 * * *")
    public void runDenormalizedSync() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(denormalizedSyncJob, params);

        } catch (Exception e) {
            log.error("Denormalized sync batch failed", e);
        }
    }
}
