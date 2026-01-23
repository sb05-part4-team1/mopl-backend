package com.mopl.batch.collect.tsdb.service.league;

import com.mopl.domain.model.league.LeagueModel;
import com.mopl.domain.repository.league.LeagueRepository;
import com.mopl.external.tsdb.client.TsdbClient;
import com.mopl.external.tsdb.model.LeagueItem;
import com.mopl.external.tsdb.model.LeagueResponse;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TsdbLeagueSyncTxService {

    private final TsdbClient tsdbClient;
    private final LeagueRepository leagueRepository;

    @Transactional
    public int syncAll() {
        Set<Long> processedLeagueIds = new HashSet<>();

        int inserted = 0;

        inserted += sync(tsdbClient.fetchAllLeagues(), processedLeagueIds);

        return inserted;
    }

    private int sync(LeagueResponse response, Set<Long> processedLeagueIds) {
        if (response == null || response.leagues() == null) {
            log.debug("TSDB league response empty");
            return 0;
        }

        int inserted = 0;

        for (LeagueItem item : response.leagues()) {
            if (item == null) {
                log.debug("TSDB invalid league skipped: reason=null item");
                continue;
            }

            Long leagueId = item.idLeague();
            String leagueName = item.strLeague();

            if (leagueId == null) {
                log.debug("TSDB invalid league skipped: leagueName={}",
                    leagueName);
                continue;
            }

            if (!processedLeagueIds.add(leagueId)) {
                log.debug("TSDB league duplicated in response skipped: leagueId={}, leagueName={}",
                    leagueId,
                    leagueName);
                continue;
            }

            try {
                if (leagueRepository.existsByLeagueId(leagueId)) {
                    log.debug("TSDB league already exists skipped: leagueId={}, leagueName={}",
                        leagueId,
                        leagueName);
                    continue;
                }

                leagueRepository.save(
                    LeagueModel.create(
                        leagueId,
                        leagueName,
                        item.strSport()
                    )
                );
                inserted++;

            } catch (DataIntegrityViolationException e) {
                log.debug("TSDB league duplicate skipped: leagueId={}, leagueName={}",
                    leagueId,
                    leagueName);

            } catch (RuntimeException e) {
                log.warn("TSDB league sync failed: leagueId={}, leagueName={}, reason={}",
                    leagueId,
                    leagueName,
                    e.getMessage());
            }
        }

        return inserted;
    }
}
