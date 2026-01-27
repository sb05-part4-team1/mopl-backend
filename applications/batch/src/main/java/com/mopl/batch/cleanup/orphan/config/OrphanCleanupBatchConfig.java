package com.mopl.batch.cleanup.orphan.config;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrphanCleanupBatchConfig {

    private final OrphanCleanupService orphanCleanupService;

    @Bean
    public Job orphanCleanupJob(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new JobBuilder("orphanCleanupJob", jobRepository)
            .start(orphanPlaylistStep(jobRepository, txManager))
            .next(orphanReviewStep(jobRepository, txManager))
            .next(orphanPlaylistSubscriberStep(jobRepository, txManager))
            .next(orphanPlaylistContentStep(jobRepository, txManager))
            .next(orphanContentTagStep(jobRepository, txManager))
            .next(orphanContentExternalMappingStep(jobRepository, txManager))
            .next(orphanNotificationStep(jobRepository, txManager))
            .next(orphanFollowStep(jobRepository, txManager))
            .next(orphanReadStatusStep(jobRepository, txManager))
            .next(orphanDirectMessageStep(jobRepository, txManager))
            .next(orphanConversationStep(jobRepository, txManager))
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
            int deleted = orphanCleanupService.cleanupPlaylists();
            log.info("[OrphanCleanup] playlist end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
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
            int deleted = orphanCleanupService.cleanupReviews();
            log.info("[OrphanCleanup] review end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
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
            int deleted = orphanCleanupService.cleanupPlaylistSubscribers();
            log.info("[OrphanCleanup] playlistSubscriber end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
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
            int deleted = orphanCleanupService.cleanupPlaylistContents();
            log.info("[OrphanCleanup] playlistContent end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
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
            int deleted = orphanCleanupService.cleanupContentTags();
            log.info("[OrphanCleanup] contentTag end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
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
            int deleted = orphanCleanupService.cleanupContentExternalMappings();
            log.info("[OrphanCleanup] contentExternalMapping end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
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
            int deleted = orphanCleanupService.cleanupNotifications();
            log.info("[OrphanCleanup] notification end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
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
            int deleted = orphanCleanupService.cleanupFollows();
            log.info("[OrphanCleanup] follow end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
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
            int deleted = orphanCleanupService.cleanupReadStatuses();
            log.info("[OrphanCleanup] readStatus end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
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
            int deleted = orphanCleanupService.cleanupDirectMessages();
            log.info("[OrphanCleanup] directMessage end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }

    // ==================== 11. Conversation ====================
    @Bean
    public Step orphanConversationStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("orphanConversationStep", jobRepository)
            .tasklet(orphanConversationTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet orphanConversationTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();
            log.info("[OrphanCleanup] conversation start");
            int deleted = orphanCleanupService.cleanupConversations();
            log.info("[OrphanCleanup] conversation end deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - start);
            return RepeatStatus.FINISHED;
        };
    }
}
