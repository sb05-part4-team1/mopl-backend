package com.mopl.batch.tsdb.scheduler;

import com.mopl.batch.tsdb.job.TsdbLeagueEventJob;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TsdbLeagueEventScheduler {

    private final TsdbLeagueEventJob job;

    @Scheduled(cron = "0 30 2 * * *")
    public void run() {
        job.run();
    }
}
