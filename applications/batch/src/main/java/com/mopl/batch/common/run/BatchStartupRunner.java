package com.mopl.batch.common.run;

import com.mopl.batch.common.config.BatchStartupProperties;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "mopl.batch", name = "run-on-startup.enabled", havingValue = "true")
@EnableConfigurationProperties(BatchStartupProperties.class)
@RequiredArgsConstructor
public class BatchStartupRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobs;
    private final BatchStartupProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        List<String> jobNames = properties.getRunOnStartup().getJobs();

        if (jobNames.isEmpty()) {
            LogContext.with("runner", "batchStartup").info("No jobs configured to run on startup");
            return;
        }

        LogContext.with("runner", "batchStartup")
            .and("jobCount", jobNames.size())
            .and("jobs", jobNames)
            .info("Starting jobs");

        for (String jobName : jobNames) {
            runJob(jobName);
        }

        LogContext.with("runner", "batchStartup").info("All startup jobs completed");
    }

    private void runJob(String jobName) {
        Job job = jobs.get(jobName);
        if (job == null) {
            LogContext.with("runner", "batchStartup")
                .and("jobName", jobName)
                .and("available", jobs.keySet())
                .warn("Job not found");
            return;
        }

        try {
            LogContext.with("runner", "batchStartup")
                .and("jobName", jobName)
                .info("Running job");
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(job, params);
            LogContext.with("runner", "batchStartup")
                .and("jobName", jobName)
                .info("Job completed");
        } catch (Exception e) {
            LogContext.with("runner", "batchStartup")
                .and("jobName", jobName)
                .error("Job failed", e);
        }
    }
}
