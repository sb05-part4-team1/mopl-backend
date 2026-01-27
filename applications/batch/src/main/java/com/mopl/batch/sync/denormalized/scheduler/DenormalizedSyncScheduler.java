package com.mopl.batch.sync.denormalized.scheduler;

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
public class DenormalizedSyncScheduler {

    private final JobLauncher jobLauncher;
    private final Job denormalizedSyncJob;

    @Scheduled(cron = "0 0 3 * * *")
    public void runDenormalizedSync() {
        LogContext.with("job", "denormalizedSync").info("Scheduled job started");
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(denormalizedSyncJob, params);
        } catch (Exception e) {
            LogContext.with("job", "denormalizedSync").error("Batch failed", e);
        }
    }
}
