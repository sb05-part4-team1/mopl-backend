package com.mopl.batch.cleanup.orphan.config;

import com.mopl.batch.cleanup.orphan.properties.OrphanCleanupProperties;
import com.mopl.batch.cleanup.orphan.service.OrphanCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OrphanCleanupProperties.class)
public class OrphanCleanupBatchConfig {

    private final OrphanCleanupService orphanCleanupService;

    @Bean
    public Job orphanCleanupJob(JobRepository jobRepository, PlatformTransactionManager txManager) {
        // 부모 엔티티 먼저 삭제 → 자식 엔티티 나중에 삭제 (cascade 효율화)
        return new JobBuilder("orphanCleanupJob", jobRepository)
            // 1. 부모 엔티티 삭제 (User, Content, Playlist, Conversation 참조)
            .start(orphanPlaylistStep(jobRepository, txManager))
            .next(orphanReviewStep(jobRepository, txManager))
            // 2. 자식 엔티티 삭제 (위에서 삭제된 부모의 하위 데이터)
            .next(orphanPlaylistSubscriberStep(jobRepository, txManager))
            .next(orphanPlaylistContentStep(jobRepository, txManager))
            .next(orphanContentTagStep(jobRepository, txManager))
            .next(orphanContentExternalMappingStep(jobRepository, txManager))
            // 3. User 참조 엔티티 (User hard delete 시 orphan 발생)
            .next(orphanNotificationStep(jobRepository, txManager))
            .next(orphanFollowStep(jobRepository, txManager))
            .next(orphanReadStatusStep(jobRepository, txManager))
            .next(orphanDirectMessageStep(jobRepository, txManager))
            .build();
    }

    // ==================== 1. Playlist ====================
    @Bean
    public Step orphanPlaylistStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanPlaylistStep", jobRepository)
            .tasklet(orphanPlaylistTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanPlaylistTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[OrphanCleanup] playlist start");
            int processed = orphanCleanupService.cleanupPlaylists();
            log.info("[OrphanCleanup] playlist end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 2. Review ====================
    @Bean
    public Step orphanReviewStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanReviewStep", jobRepository)
            .tasklet(orphanReviewTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanReviewTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[OrphanCleanup] review start");
            int processed = orphanCleanupService.cleanupReviews();
            log.info("[OrphanCleanup] review end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 3. PlaylistSubscriber ====================
    @Bean
    public Step orphanPlaylistSubscriberStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanPlaylistSubscriberStep", jobRepository)
            .tasklet(orphanPlaylistSubscriberTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanPlaylistSubscriberTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[OrphanCleanup] playlistSubscriber start");
            int processed = orphanCleanupService.cleanupPlaylistSubscribers();
            log.info("[OrphanCleanup] playlistSubscriber end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 4. PlaylistContent ====================
    @Bean
    public Step orphanPlaylistContentStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanPlaylistContentStep", jobRepository)
            .tasklet(orphanPlaylistContentTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanPlaylistContentTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[OrphanCleanup] playlistContent start");
            int processed = orphanCleanupService.cleanupPlaylistContents();
            log.info("[OrphanCleanup] playlistContent end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 5. ContentTag ====================
    @Bean
    public Step orphanContentTagStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanContentTagStep", jobRepository)
            .tasklet(orphanContentTagTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanContentTagTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[OrphanCleanup] contentTag start");
            int processed = orphanCleanupService.cleanupContentTags();
            log.info("[OrphanCleanup] contentTag end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 6. ContentExternalMapping ====================
    @Bean
    public Step orphanContentExternalMappingStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanContentExternalMappingStep", jobRepository)
            .tasklet(orphanContentExternalMappingTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanContentExternalMappingTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[OrphanCleanup] contentExternalMapping start");
            int processed = orphanCleanupService.cleanupContentExternalMappings();
            log.info("[OrphanCleanup] contentExternalMapping end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 7. Notification ====================
    @Bean
    public Step orphanNotificationStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanNotificationStep", jobRepository)
            .tasklet(orphanNotificationTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanNotificationTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[OrphanCleanup] notification start");
            int processed = orphanCleanupService.cleanupNotifications();
            log.info("[OrphanCleanup] notification end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 8. Follow ====================
    @Bean
    public Step orphanFollowStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanFollowStep", jobRepository)
            .tasklet(orphanFollowTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanFollowTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[OrphanCleanup] follow start");
            int processed = orphanCleanupService.cleanupFollows();
            log.info("[OrphanCleanup] follow end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 9. ReadStatus ====================
    @Bean
    public Step orphanReadStatusStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanReadStatusStep", jobRepository)
            .tasklet(orphanReadStatusTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanReadStatusTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[OrphanCleanup] readStatus start");
            int processed = orphanCleanupService.cleanupReadStatuses();
            log.info("[OrphanCleanup] readStatus end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 10. DirectMessage ====================
    @Bean
    public Step orphanDirectMessageStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanDirectMessageStep", jobRepository)
            .tasklet(orphanDirectMessageTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanDirectMessageTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[OrphanCleanup] directMessage start");
            int processed = orphanCleanupService.cleanupDirectMessages();
            log.info("[OrphanCleanup] directMessage end processed={} durationMs={}",
                processed, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }
}
