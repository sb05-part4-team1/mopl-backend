package com.mopl.batch.cleanup.orphanstorage.scheduler;

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
public class OrphanStorageCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job orphanStorageCleanupJob;

    @Scheduled(cron = "0 0 5 * * *")
    public void runOrphanStorageCleanup() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(orphanStorageCleanupJob, params);

        } catch (Exception e) {
            log.error("Orphan storage cleanup batch failed", e);
        }
    }
}
