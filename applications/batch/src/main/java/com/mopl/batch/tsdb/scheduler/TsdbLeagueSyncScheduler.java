package com.mopl.batch.tsdb.scheduler;

import com.mopl.batch.tsdb.job.TsdbLeagueSyncJob;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TsdbLeagueSyncScheduler {

    private final TsdbLeagueSyncJob job;

    @Scheduled(cron = "0 0 4 * * MON")
    public void runLeagueSync() {
        job.run();
    }
}
