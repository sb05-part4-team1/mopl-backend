package com.mopl.batch.tsdb.job;

import com.mopl.batch.tsdb.service.TsdbLeagueEventCollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TsdbLeagueEventJob {

    private final TsdbLeagueEventCollectService collectService;

    public void run() {
        collectService.collectAll();
    }
}
