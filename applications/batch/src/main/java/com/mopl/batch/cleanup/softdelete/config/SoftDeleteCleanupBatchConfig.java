package com.mopl.batch.cleanup.softdelete.config;

import com.mopl.batch.cleanup.softdelete.service.content.ContentCleanupService;
import com.mopl.batch.cleanup.softdelete.service.log.ContentDeletionLogCleanupService;
import com.mopl.batch.cleanup.softdelete.service.storage.StorageCleanupService;
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
public class SoftDeleteCleanupBatchConfig {

    private final ContentCleanupService contentCleanupService;
    private final StorageCleanupService storageCleanupService;
    private final ContentDeletionLogCleanupService contentDeletionLogCleanupService;

    @Bean
    public Job softDeleteCleanupJob(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new JobBuilder("softDeleteCleanupJob", jobRepository)
            .start(contentStep(jobRepository, txManager))
            .next(storageStep(jobRepository, txManager))
            .next(deletionLogStep(jobRepository, txManager))
            .build();
    }

    // ==================== 1. Content ====================
    @Bean
    public Step contentStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("contentStep", jobRepository)
            .tasklet(contentTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet contentTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            LogContext.with("task", "content").info("[SoftDeleteCleanup] start");
            int processed = contentCleanupService.cleanup();
            LogContext.with("task", "content")
                .and("processed", processed)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[SoftDeleteCleanup] end");
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 2. Storage ====================
    @Bean
    public Step storageStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("storageStep", jobRepository)
            .tasklet(storageTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet storageTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            LogContext.with("task", "storage").info("[SoftDeleteCleanup] start");
            int deletedFiles = storageCleanupService.cleanup();
            LogContext.with("task", "storage")
                .and("deletedFiles", deletedFiles)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[SoftDeleteCleanup] end");
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 3. DeletionLog ====================
    @Bean
    public Step deletionLogStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("deletionLogStep", jobRepository)
            .tasklet(deletionLogTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet deletionLogTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            LogContext.with("task", "deletionLog").info("[SoftDeleteCleanup] start");
            int processed = contentDeletionLogCleanupService.cleanup();
            LogContext.with("task", "deletionLog")
                .and("processed", processed)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[SoftDeleteCleanup] end");
            return RepeatStatus.FINISHED;
        };
    }
}
