package com.mopl.batch.collect.tsdb.initializer;

import com.mopl.batch.collect.tsdb.service.league.TsdbLeagueSyncTxService;
import com.mopl.domain.repository.league.LeagueRepository;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TsdbLeagueInitializer implements CommandLineRunner {

    private final TsdbLeagueSyncTxService syncService;
    private final LeagueRepository leagueRepository;

    @Override
    public void run(String... args) {

        if (leagueRepository.count() == 0) {
            LogContext.with("initializer", "tsdbLeague").info("League table empty, initializing");
            syncService.syncAll();
        } else {
            LogContext.with("initializer", "tsdbLeague").info("League table already initialized, skipping");
        }
    }
}
