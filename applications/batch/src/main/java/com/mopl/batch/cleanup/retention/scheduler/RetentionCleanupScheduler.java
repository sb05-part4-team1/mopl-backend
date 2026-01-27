package com.mopl.batch.cleanup.retention.scheduler;

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
public class RetentionCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job retentionCleanupJob;

    @Scheduled(cron = "0 0 4 * * *")
    public void runCleanup() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(retentionCleanupJob, params);

        } catch (Exception e) {
            log.error("Retention cleanup batch failed", e);
        }
    }
}
