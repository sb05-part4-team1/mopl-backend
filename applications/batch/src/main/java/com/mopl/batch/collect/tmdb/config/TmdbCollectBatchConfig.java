package com.mopl.batch.collect.tmdb.config;

import com.mopl.batch.collect.tmdb.service.genre.TmdbGenreSyncTxService;
import com.mopl.batch.collect.tmdb.service.content.TmdbPopularMovieContentCollectService;
import com.mopl.batch.collect.tmdb.service.content.TmdbPopularTvContentCollectService;
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
public class TmdbCollectBatchConfig {

    private final TmdbPopularMovieContentCollectService tmdbPopularMovieService;
    private final TmdbPopularTvContentCollectService tmdbPopularTvService;
    private final TmdbGenreSyncTxService tmdbGenreSyncTxService;

    @Bean
    public Job tmdbCollectJob(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new JobBuilder("tmdbCollectJob", jobRepository)
            .start(tmdbPopularMovieStep(jobRepository, txManager))
            .next(tmdbPopularTvStep(jobRepository, txManager))
            .build();
    }

    @Bean
    public Job tmdbGenreSyncJob(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new JobBuilder("tmdbGenreSyncJob", jobRepository)
            .start(tmdbGenreSyncStep(jobRepository, txManager))
            .build();
    }

    @Bean
    public Step tmdbPopularMovieStep(JobRepository jobRepository,
        PlatformTransactionManager txManager) {
        return new StepBuilder("tmdbPopularMovieStep", jobRepository)
            .tasklet(tmdbPopularMovieTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet tmdbPopularMovieTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();

            log.info("[TMDB Collect] movieContent start");
            int processed = tmdbPopularMovieService.collectPopularMovies();
            log.info("[TMDB Collect] movieContent end processed={} durationMs={}", processed, System
                .currentTimeMillis() - start);

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step tmdbPopularTvStep(JobRepository jobRepository,
        PlatformTransactionManager txManager) {
        return new StepBuilder("tmdbPopularTvStep", jobRepository)
            .tasklet(tmdbPopularTvTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet tmdbPopularTvTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();

            log.info("[TMDB Collect] tvContent start");
            int processed = tmdbPopularTvService.collectPopularTvSeries();
            log.info("[TMDB Collect] tvContent end processed={} durationMs={}", processed, System
                .currentTimeMillis() - start);

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step tmdbGenreSyncStep(JobRepository jobRepository,
        PlatformTransactionManager txManager) {
        return new StepBuilder("tmdbGenreSyncStep", jobRepository)
            .tasklet(tmdbGenreSyncTasklet(), txManager)
            .build();
    }

    @Bean
    public Tasklet tmdbGenreSyncTasklet() {
        return (contribution, chunkContext) -> {
            long start = System.currentTimeMillis();

            log.info("[TMDB GenreSync] start");
            int inserted = tmdbGenreSyncTxService.syncAll();
            log.info("[TMDB GenreSync] end inserted={} durationMs={}", inserted, System
                .currentTimeMillis() - start);

            return RepeatStatus.FINISHED;
        };
    }
}
