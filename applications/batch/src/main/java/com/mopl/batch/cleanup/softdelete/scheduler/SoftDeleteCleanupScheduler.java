package com.mopl.batch.cleanup.softdelete.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SoftDeleteCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job softDeleteCleanupJob;

    @Scheduled(cron = "0 0 4 * * *")
    public void runCleanup() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(softDeleteCleanupJob, params);

        } catch (Exception e) {
            log.error("Soft delete cleanup batch failed", e);
        }
    }
}
