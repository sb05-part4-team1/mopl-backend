package com.mopl.batch.collect.tsdb.scheduler;

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
@DisplayName("TsdbCollectScheduler 단위 테스트")
class TsdbCollectSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job tsdbLeagueEventCollectJob;

    @Mock
    private Job tsdbLeagueSyncJob;

    @Captor
    private ArgumentCaptor<JobParameters> jobParametersCaptor;

    private TsdbCollectScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TsdbCollectScheduler(jobLauncher, tsdbLeagueEventCollectJob, tsdbLeagueSyncJob);
    }

    @Nested
    @DisplayName("runTsdbCollect()")
    class RunTsdbCollectTest {

        @Test
        @DisplayName("스케줄러가 tsdbLeagueEventCollectJob을 실행한다")
        void runsTsdbLeagueEventCollectJob() throws Exception {
            JobExecution jobExecution = mock(JobExecution.class);
            when(jobLauncher.run(eq(tsdbLeagueEventCollectJob), any(JobParameters.class))).thenReturn(jobExecution);

            scheduler.runTsdbCollect();

            verify(jobLauncher).run(eq(tsdbLeagueEventCollectJob), jobParametersCaptor.capture());
            JobParameters params = jobParametersCaptor.getValue();
            assertThat(params.getLong("time")).isNotNull();
        }

        @Test
        @DisplayName("Job 실행 중 예외가 발생해도 스케줄러는 예외를 던지지 않는다")
        void handlesExceptionGracefully() throws Exception {
            when(jobLauncher.run(eq(tsdbLeagueEventCollectJob), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Test exception"));

            scheduler.runTsdbCollect();

            verify(jobLauncher).run(eq(tsdbLeagueEventCollectJob), any(JobParameters.class));
        }
    }

    @Nested
    @DisplayName("runTsdbLeagueSync()")
    class RunTsdbLeagueSyncTest {

        @Test
        @DisplayName("스케줄러가 tsdbLeagueSyncJob을 실행한다")
        void runsTsdbLeagueSyncJob() throws Exception {
            JobExecution jobExecution = mock(JobExecution.class);
            when(jobLauncher.run(eq(tsdbLeagueSyncJob), any(JobParameters.class))).thenReturn(jobExecution);

            scheduler.runTsdbLeagueSync();

            verify(jobLauncher).run(eq(tsdbLeagueSyncJob), jobParametersCaptor.capture());
            JobParameters params = jobParametersCaptor.getValue();
            assertThat(params.getLong("time")).isNotNull();
        }

        @Test
        @DisplayName("Job 실행 중 예외가 발생해도 스케줄러는 예외를 던지지 않는다")
        void handlesExceptionGracefully() throws Exception {
            when(jobLauncher.run(eq(tsdbLeagueSyncJob), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Test exception"));

            scheduler.runTsdbLeagueSync();

            verify(jobLauncher).run(eq(tsdbLeagueSyncJob), any(JobParameters.class));
        }
    }
}
