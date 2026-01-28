package com.mopl.batch.cleanup.orphanstorage.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
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
class OrphanStorageCleanupSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job orphanStorageCleanupJob;

    @InjectMocks
    private OrphanStorageCleanupScheduler scheduler;

    @Captor
    private ArgumentCaptor<JobParameters> jobParametersCaptor;

    @Test
    @DisplayName("스케줄러가 orphanStorageCleanupJob을 실행한다")
    void runsOrphanStorageCleanupJob() throws Exception {
        JobExecution jobExecution = mock(JobExecution.class);
        when(jobLauncher.run(eq(orphanStorageCleanupJob), any(JobParameters.class))).thenReturn(jobExecution);

        scheduler.runOrphanStorageCleanup();

        verify(jobLauncher).run(eq(orphanStorageCleanupJob), jobParametersCaptor.capture());
        JobParameters params = jobParametersCaptor.getValue();
        assertThat(params.getLong("time")).isNotNull();
    }

    @Test
    @DisplayName("Job 실행 중 예외가 발생해도 스케줄러는 예외를 던지지 않는다")
    void handlesExceptionGracefully() throws Exception {
        when(jobLauncher.run(eq(orphanStorageCleanupJob), any(JobParameters.class)))
            .thenThrow(new RuntimeException("Test exception"));

        scheduler.runOrphanStorageCleanup();

        verify(jobLauncher).run(eq(orphanStorageCleanupJob), any(JobParameters.class));
    }
}
