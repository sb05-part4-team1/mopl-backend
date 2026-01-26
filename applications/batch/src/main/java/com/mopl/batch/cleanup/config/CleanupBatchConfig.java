package com.mopl.batch.cleanup.config;

import com.mopl.batch.cleanup.service.content.ContentCleanupService;
import com.mopl.batch.cleanup.service.log.ContentDeletionLogCleanupService;
import com.mopl.batch.cleanup.service.notification.NotificationCleanupService;
import com.mopl.batch.cleanup.service.playlist.PlaylistCleanupService;
import com.mopl.batch.cleanup.service.review.ReviewCleanupService;
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
    private final PlaylistCleanupService playlistCleanupService;
    private final ReviewCleanupService reviewCleanupService;
    private final NotificationCleanupService notificationCleanupService;
    private final StorageCleanupService storageCleanupService;
    private final ContentDeletionLogCleanupService contentDeletionLogCleanupService;

    @Bean
    public Job cleanupJob(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new JobBuilder("cleanupJob", jobRepository)
            // 1. 부모 엔티티 삭제
            .start(contentStep(jobRepository, txManager))
            .next(playlistStep(jobRepository, txManager))
            // 2. 자식 엔티티 삭제 (soft delete된 것 정리)
            .next(reviewStep(jobRepository, txManager))
            .next(notificationStep(jobRepository, txManager))
            // 3. 기타 정리
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

    // ==================== 2. Playlist ====================
    @Bean
    public Step playlistStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("playlistStep", jobRepository)
            .tasklet(playlistTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet playlistTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[Cleanup] playlist start");
            int processed = playlistCleanupService.cleanup();
            log.info("[Cleanup] playlist end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 3. Review ====================
    @Bean
    public Step reviewStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("reviewStep", jobRepository)
            .tasklet(reviewTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet reviewTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[Cleanup] review start");
            int processed = reviewCleanupService.cleanup();
            log.info("[Cleanup] review end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 4. Notification ====================
    @Bean
    public Step notificationStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("notificationStep", jobRepository)
            .tasklet(notificationTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet notificationTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[Cleanup] notification start");
            int processed = notificationCleanupService.cleanup();
            log.info("[Cleanup] notification end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 5. Storage ====================
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

    // ==================== 6. DeletionLog ====================
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
