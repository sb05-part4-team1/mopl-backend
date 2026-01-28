package com.mopl.batch.collect.tmdb.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TmdbCollectScheduler 단위 테스트")
class TmdbCollectSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job tmdbCollectJob;

    @Mock
    private Job tmdbGenreSyncJob;

    @Captor
    private ArgumentCaptor<JobParameters> jobParametersCaptor;

    private TmdbCollectScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TmdbCollectScheduler(jobLauncher, tmdbCollectJob, tmdbGenreSyncJob);
    }

    @Nested
    @DisplayName("runTmdbCollect()")
    class RunTmdbCollectTest {

        @Test
        @DisplayName("스케줄러가 tmdbCollectJob을 실행한다")
        void runsTmdbCollectJob() throws Exception {
            JobExecution jobExecution = mock(JobExecution.class);
            when(jobLauncher.run(eq(tmdbCollectJob), any(JobParameters.class))).thenReturn(jobExecution);

            scheduler.runTmdbCollect();

            verify(jobLauncher).run(eq(tmdbCollectJob), jobParametersCaptor.capture());
            JobParameters params = jobParametersCaptor.getValue();
            assertThat(params.getLong("time")).isNotNull();
        }

        @Test
        @DisplayName("Job 실행 중 예외가 발생해도 스케줄러는 예외를 던지지 않는다")
        void handlesExceptionGracefully() throws Exception {
            when(jobLauncher.run(eq(tmdbCollectJob), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Test exception"));

            scheduler.runTmdbCollect();

            verify(jobLauncher).run(eq(tmdbCollectJob), any(JobParameters.class));
        }
    }

    @Nested
    @DisplayName("runTmdbGenreSync()")
    class RunTmdbGenreSyncTest {

        @Test
        @DisplayName("스케줄러가 tmdbGenreSyncJob을 실행한다")
        void runsTmdbGenreSyncJob() throws Exception {
            JobExecution jobExecution = mock(JobExecution.class);
            when(jobLauncher.run(eq(tmdbGenreSyncJob), any(JobParameters.class))).thenReturn(jobExecution);

            scheduler.runTmdbGenreSync();

            verify(jobLauncher).run(eq(tmdbGenreSyncJob), jobParametersCaptor.capture());
            JobParameters params = jobParametersCaptor.getValue();
            assertThat(params.getLong("time")).isNotNull();
        }

        @Test
        @DisplayName("Job 실행 중 예외가 발생해도 스케줄러는 예외를 던지지 않는다")
        void handlesExceptionGracefully() throws Exception {
            when(jobLauncher.run(eq(tmdbGenreSyncJob), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Test exception"));

            scheduler.runTmdbGenreSync();

            verify(jobLauncher).run(eq(tmdbGenreSyncJob), any(JobParameters.class));
        }
    }
}
