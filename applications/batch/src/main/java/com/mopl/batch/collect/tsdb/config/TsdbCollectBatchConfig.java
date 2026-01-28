package com.mopl.batch.collect.tsdb.config;

import com.mopl.batch.collect.tsdb.service.content.TsdbNextLeagueEventCollectService;
import com.mopl.batch.collect.tsdb.service.content.TsdbPastLeagueEventCollectService;
import com.mopl.batch.collect.tsdb.service.league.TsdbLeagueSyncTxService;
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
public class TsdbCollectBatchConfig {

    private final TsdbPastLeagueEventCollectService tsdbPastLeagueEventService;
    private final TsdbNextLeagueEventCollectService tsdbNextLeagueEventService;
    private final TsdbLeagueSyncTxService tsdbLeagueSyncTxService;

    @Bean
    public Job tsdbLeagueEventCollectJob(
        JobRepository jobRepository,
        PlatformTransactionManager txManager
    ) {
        return new JobBuilder("tsdbLeagueEventCollectJob", jobRepository)
            .start(tsdbPastLeagueEventStep(jobRepository, txManager))
            .next(tsdbNextLeagueEventStep(jobRepository, txManager))
            .build();
    }

    @Bean
    public Job tsdbLeagueSyncJob(
        JobRepository jobRepository,
        PlatformTransactionManager txManager
    ) {
        return new JobBuilder("tsdbLeagueSyncJob", jobRepository)
            .start(tsdbLeagueSyncStep(jobRepository, txManager))
            .build();
    }

    @Bean
    public Step tsdbPastLeagueEventStep(
        JobRepository jobRepository,
        PlatformTransactionManager txManager
    ) {
        return new StepBuilder("tsdbPastLeagueEventStep", jobRepository)
            .tasklet(tsdbPastLeagueEventTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet tsdbPastLeagueEventTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();

            LogContext.with("task", "pastLeagueEvent").info("[TSDB Collect] start");
            int processed = tsdbPastLeagueEventService.collectPastLeagueEvents();
            LogContext.with("task", "pastLeagueEvent")
                .and("processed", processed)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[TSDB Collect] end");

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step tsdbNextLeagueEventStep(
        JobRepository jobRepository,
        PlatformTransactionManager txManager
    ) {
        return new StepBuilder("tsdbNextLeagueEventStep", jobRepository)
            .tasklet(tsdbNextLeagueEventTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet tsdbNextLeagueEventTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();

            LogContext.with("task", "nextLeagueEvent").info("[TSDB Collect] start");
            int processed = tsdbNextLeagueEventService.collectNextLeagueEvents();
            LogContext.with("task", "nextLeagueEvent")
                .and("processed", processed)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[TSDB Collect] end");

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step tsdbLeagueSyncStep(
        JobRepository jobRepository,
        PlatformTransactionManager txManager
    ) {
        return new StepBuilder("tsdbLeagueSyncStep", jobRepository)
            .tasklet(tsdbLeagueSyncTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet tsdbLeagueSyncTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();

            LogContext.with("task", "leagueSync").info("[TSDB LeagueSync] start");
            int inserted = tsdbLeagueSyncTxService.syncAll();
            LogContext.with("task", "leagueSync")
                .and("inserted", inserted)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[TSDB LeagueSync] end");

            return RepeatStatus.FINISHED;
        };
    }
}
