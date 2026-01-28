package com.mopl.batch.common.run;

import com.mopl.batch.common.config.BatchStartupProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class BatchStartupRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobs;
    private final BatchStartupProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        List<String> jobNames = properties.getRunOnStartup().getJobs();

        if (jobNames.isEmpty()) {
            log.info("[BatchStartup] No jobs configured to run on startup");
            return;
        }

        log.info("[BatchStartup] Starting {} jobs: {}", jobNames.size(), jobNames);

        for (String jobName : jobNames) {
            runJob(jobName);
        }

        log.info("[BatchStartup] All startup jobs completed");
    }

    private void runJob(String jobName) {
        Job job = jobs.get(jobName);
        if (job == null) {
            log.warn("[BatchStartup] Job not found: {}. Available: {}", jobName, jobs.keySet());
            return;
        }

        try {
            log.info("[BatchStartup] Running job: {}", jobName);
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(job, params);
            log.info("[BatchStartup] Job completed: {}", jobName);
        } catch (Exception e) {
            log.error("[BatchStartup] Job failed: {}", jobName, e);
        }
    }
}
