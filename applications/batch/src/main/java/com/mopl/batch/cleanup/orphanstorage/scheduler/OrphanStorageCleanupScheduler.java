package com.mopl.batch.cleanup.orphanstorage.scheduler;

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
public class OrphanStorageCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job orphanStorageCleanupJob;

    @Scheduled(cron = "0 0 5 * * *")
    public void runOrphanStorageCleanup() {
        LogContext.with("job", "orphanStorageCleanup").info("Scheduled job started");
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(orphanStorageCleanupJob, params);
        } catch (Exception e) {
            LogContext.with("job", "orphanStorageCleanup").error("Batch failed", e);
        }
    }
}
