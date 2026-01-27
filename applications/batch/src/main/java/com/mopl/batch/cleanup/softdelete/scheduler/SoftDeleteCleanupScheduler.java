package com.mopl.batch.cleanup.softdelete.scheduler;

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
public class SoftDeleteCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job softDeleteCleanupJob;

    @Scheduled(cron = "0 0 4 * * *")
    public void runCleanup() {
        LogContext.with("job", "softDeleteCleanup").info("Scheduled job started");
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(softDeleteCleanupJob, params);
        } catch (Exception e) {
            LogContext.with("job", "softDeleteCleanup").error("Batch failed", e);
        }
    }
}
