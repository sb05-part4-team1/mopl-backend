package com.mopl.batch.sync.denormalized.config;

import com.mopl.batch.sync.denormalized.service.ContentReviewStatsSyncService;
import com.mopl.batch.sync.denormalized.service.PlaylistSubscriberCountSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DenormalizedSyncBatchConfig {

    private final PlaylistSubscriberCountSyncService playlistSubscriberCountSyncService;
    private final ContentReviewStatsSyncService contentReviewStatsSyncService;

    @Bean
    public Job denormalizedSyncJob(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new JobBuilder("denormalizedSyncJob", jobRepository)
            .start(playlistSubscriberCountSyncStep(jobRepository, txManager))
            .next(contentReviewStatsSyncStep(jobRepository, txManager))
            .build();
    }

    @Bean
    public Step playlistSubscriberCountSyncStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("playlistSubscriberCountSyncStep", jobRepository)
            .tasklet(playlistSubscriberCountSyncTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet playlistSubscriberCountSyncTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();

            log.info("[DenormalizedSync] playlist subscriber count sync start");
            int synced = playlistSubscriberCountSyncService.sync();
            log.info("[DenormalizedSync] playlist subscriber count sync end synced={} durationMs={}",
                synced, System.currentTimeMillis() - start);

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step contentReviewStatsSyncStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("contentReviewStatsSyncStep", jobRepository)
            .tasklet(contentReviewStatsSyncTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet contentReviewStatsSyncTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();

            log.info("[DenormalizedSync] content review stats sync start");
            int synced = contentReviewStatsSyncService.sync();
            log.info("[DenormalizedSync] content review stats sync end synced={} durationMs={}",
                synced, System.currentTimeMillis() - start);

            return RepeatStatus.FINISHED;
        };
    }
}
