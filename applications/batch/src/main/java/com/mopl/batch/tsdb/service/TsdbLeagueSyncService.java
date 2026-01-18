package com.mopl.batch.tsdb.service;

import com.mopl.domain.model.league.LeagueModel;
import com.mopl.domain.repository.league.LeagueRepository;
import com.mopl.external.tsdb.client.TsdbClient;
import com.mopl.external.tsdb.model.LeagueItem;
import com.mopl.external.tsdb.model.LeagueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TsdbLeagueSyncService {

    private final TsdbClient tsdbClient;
    private final LeagueRepository leagueRepository;

    @Transactional
    public void syncAll() {

        log.info("Start TSDB league sync");

        LeagueResponse response = tsdbClient.fetchAllLeagues();
        sync(response);

        log.info("TSDB league sync completed");
    }

    private void sync(LeagueResponse response) {
        if (response == null || response.leagues() == null) {
            return;
        }

        for (LeagueItem item : response.leagues()) {
            Long leagueId = item.idLeague();

            try {
                if (leagueRepository.existsByLeagueId(leagueId)) {
                    continue;
                }

                leagueRepository.save(
                    LeagueModel.create(
                        leagueId,
                        item.strLeague(),
                        item.strSport()
                    )
                );

            } catch (RuntimeException e) {
                log.debug(
                    "TSDB league already exists or skipped: leagueId={}, name={}",
                    leagueId,
                    item.strLeague()
                );
            }
        }
    }
}
