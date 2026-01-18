package com.mopl.batch.tsdb.initializer;

import com.mopl.batch.tsdb.service.TsdbLeagueSyncService;
import com.mopl.domain.repository.league.LeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TsdbLeagueInitializer implements CommandLineRunner {

    private final TsdbLeagueSyncService syncService;
    private final LeagueRepository leagueRepository;

    @Override
    public void run(String... args) {

        if (leagueRepository.count() == 0) {
            log.info("League table empty. Initializing TSDB leagues...");
            syncService.syncAll();
        } else {
            log.info("League table already initialized. Skip TSDB league sync.");
        }
    }
}
