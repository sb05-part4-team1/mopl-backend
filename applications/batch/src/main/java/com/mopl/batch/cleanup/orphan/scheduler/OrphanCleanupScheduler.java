package com.mopl.batch.cleanup.orphan.scheduler;

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
public class OrphanCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job orphanCleanupJob;

    @Scheduled(cron = "0 30 4 * * *")
    public void runOrphanCleanup() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(orphanCleanupJob, params);

        } catch (Exception e) {
            log.error("Orphan cleanup batch failed", e);
        }
    }
}
