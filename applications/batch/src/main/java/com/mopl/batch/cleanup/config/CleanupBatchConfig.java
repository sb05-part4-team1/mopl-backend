package com.mopl.batch.cleanup.config;

import com.mopl.batch.cleanup.service.content.ContentCleanupService;
import com.mopl.batch.cleanup.service.log.ContentDeletionLogCleanupService;
import com.mopl.batch.cleanup.service.storage.StorageCleanupService;
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
public class CleanupBatchConfig {

    private final ContentCleanupService contentCleanupService;
    private final StorageCleanupService storageCleanupService;
    private final ContentDeletionLogCleanupService contentDeletionLogCleanupService;

    @Bean
    public Job cleanupJob(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new JobBuilder("cleanupJob", jobRepository)
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
            log.info("[Cleanup] content start");
            int processed = contentCleanupService.cleanup();
            log.info("[Cleanup] content end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
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
            log.info("[Cleanup] storage start");
            int deletedFiles = storageCleanupService.cleanup();
            log.info("[Cleanup] storage end deletedFiles={} durationMs={}",
                deletedFiles, System.currentTimeMillis() - start);
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
            log.info("[Cleanup] deletionLog start");
            int processed = contentDeletionLogCleanupService.cleanup();
            log.info("[Cleanup] deletionLog end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }
}
