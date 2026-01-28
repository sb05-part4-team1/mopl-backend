package com.mopl.batch.sync.denormalized.config;

import com.mopl.batch.sync.denormalized.service.ContentReviewStatsSyncService;
import com.mopl.batch.sync.denormalized.service.PlaylistSubscriberCountSyncService;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
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

            LogContext.with("task", "playlistSubscriberCount").info("[DenormalizedSync] start");
            int synced = playlistSubscriberCountSyncService.sync();
            LogContext.with("task", "playlistSubscriberCount")
                .and("synced", synced)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[DenormalizedSync] end");

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

            LogContext.with("task", "contentReviewStats").info("[DenormalizedSync] start");
            int synced = contentReviewStatsSyncService.sync();
            LogContext.with("task", "contentReviewStats")
                .and("synced", synced)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[DenormalizedSync] end");

            return RepeatStatus.FINISHED;
        };
    }
}
