package com.mopl.batch.cleanup.orphanstorage.config;

import com.mopl.batch.cleanup.orphanstorage.service.OrphanStorageCleanupService;
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

@Configuration
@RequiredArgsConstructor
@Slf4j
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
            log.info("[OrphanStorageCleanup] contentThumbnail start");
            int deleted = orphanStorageCleanupService.cleanupContentThumbnails();
            log.info("[OrphanStorageCleanup] contentThumbnail end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
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
            log.info("[OrphanStorageCleanup] userProfileImage start");
            int deleted = orphanStorageCleanupService.cleanupUserProfileImages();
            log.info("[OrphanStorageCleanup] userProfileImage end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }
}
