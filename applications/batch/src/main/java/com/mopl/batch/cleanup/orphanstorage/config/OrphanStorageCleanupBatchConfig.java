package com.mopl.batch.cleanup.orphanstorage.config;

import com.mopl.batch.cleanup.orphanstorage.service.OrphanStorageCleanupService;
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
public class OrphanStorageCleanupBatchConfig {

    private final OrphanStorageCleanupService orphanStorageCleanupService;

    @Bean
    public Job orphanStorageCleanupJob(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new JobBuilder("orphanStorageCleanupJob", jobRepository)
            .start(orphanContentThumbnailStep(jobRepository, txManager))
            .next(orphanUserProfileImageStep(jobRepository, txManager))
            .build();
    }

    // ==================== 1. Content Thumbnail ====================
    @Bean
    public Step orphanContentThumbnailStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanContentThumbnailStep", jobRepository)
            .tasklet(orphanContentThumbnailTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanContentThumbnailTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            LogContext.with("task", "contentThumbnail").info("[OrphanStorageCleanup] start");
            int deleted = orphanStorageCleanupService.cleanupContentThumbnails();
            LogContext.with("task", "contentThumbnail")
                .and("deleted", deleted)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[OrphanStorageCleanup] end");
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 2. User Profile Image ====================
    @Bean
    public Step orphanUserProfileImageStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanUserProfileImageStep", jobRepository)
            .tasklet(orphanUserProfileImageTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanUserProfileImageTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            LogContext.with("task", "userProfileImage").info("[OrphanStorageCleanup] start");
            int deleted = orphanStorageCleanupService.cleanupUserProfileImages();
            LogContext.with("task", "userProfileImage")
                .and("deleted", deleted)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[OrphanStorageCleanup] end");
            return RepeatStatus.FINISHED;
        };
    }
}
