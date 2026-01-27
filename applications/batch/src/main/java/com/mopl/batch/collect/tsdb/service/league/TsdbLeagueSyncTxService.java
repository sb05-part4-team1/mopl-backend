package com.mopl.batch.collect.tsdb.service.league;

import com.mopl.domain.model.league.LeagueModel;
import com.mopl.domain.repository.league.LeagueRepository;
import com.mopl.external.tsdb.client.TsdbClient;
import com.mopl.external.tsdb.model.LeagueItem;
import com.mopl.external.tsdb.model.LeagueResponse;
import com.mopl.logging.context.LogContext;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            LogContext.with("service", "tsdbLeagueSync").debug("League response empty");
            return 0;
        }

        int inserted = 0;

        for (LeagueItem item : response.leagues()) {
            if (item == null) {
                LogContext.with("service", "tsdbLeagueSync")
                    .and("reason", "null item")
                    .debug("Invalid league skipped");
                continue;
            }

            Long leagueId = item.idLeague();
            String leagueName = item.strLeague();

            if (leagueId == null) {
                LogContext.with("service", "tsdbLeagueSync")
                    .and("leagueName", leagueName)
                    .debug("Invalid league skipped");
                continue;
            }

            if (!processedLeagueIds.add(leagueId)) {
                LogContext.with("service", "tsdbLeagueSync")
                    .and("leagueId", leagueId)
                    .and("leagueName", leagueName)
                    .debug("Duplicated in response skipped");
                continue;
            }

            try {
                if (leagueRepository.existsByLeagueId(leagueId)) {
                    LogContext.with("service", "tsdbLeagueSync")
                        .and("leagueId", leagueId)
                        .and("leagueName", leagueName)
                        .debug("Already exists skipped");
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
                LogContext.with("service", "tsdbLeagueSync")
                    .and("leagueId", leagueId)
                    .and("leagueName", leagueName)
                    .debug("Duplicate skipped");

            } catch (RuntimeException e) {
                LogContext.with("service", "tsdbLeagueSync")
                    .and("leagueId", leagueId)
                    .and("leagueName", leagueName)
                    .and("reason", e.getMessage())
                    .warn("Sync failed");
            }
        }

        return inserted;
    }
}
