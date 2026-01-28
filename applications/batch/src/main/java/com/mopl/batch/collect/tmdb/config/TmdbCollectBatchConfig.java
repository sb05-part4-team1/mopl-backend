package com.mopl.batch.collect.tmdb.config;

import com.mopl.batch.collect.tmdb.service.content.TmdbPopularMovieContentCollectService;
import com.mopl.batch.collect.tmdb.service.content.TmdbPopularTvContentCollectService;
import com.mopl.batch.collect.tmdb.service.genre.TmdbGenreSyncTxService;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.Step;

@Configuration
@RequiredArgsConstructor
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

            LogContext.with("task", "movieContent").info("[TMDB Collect] start");
            int processed = tmdbPopularMovieService.collectPopularMovies();
            LogContext.with("task", "movieContent")
                .and("processed", processed)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[TMDB Collect] end");

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

            LogContext.with("task", "tvContent").info("[TMDB Collect] start");
            int processed = tmdbPopularTvService.collectPopularTvSeries();
            LogContext.with("task", "tvContent")
                .and("processed", processed)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[TMDB Collect] end");

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

            LogContext.with("task", "genreSync").info("[TMDB GenreSync] start");
            int inserted = tmdbGenreSyncTxService.syncAll();
            LogContext.with("task", "genreSync")
                .and("inserted", inserted)
                .and("durationMs", System.currentTimeMillis() - start)
                .info("[TMDB GenreSync] end");

            return RepeatStatus.FINISHED;
        };
    }
}
