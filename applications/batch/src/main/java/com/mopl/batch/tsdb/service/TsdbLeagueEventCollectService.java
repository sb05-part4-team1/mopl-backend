package com.mopl.batch.tsdb.service;

import com.mopl.domain.repository.league.LeagueRepository;
import com.mopl.external.tsdb.client.TsdbClient;
import com.mopl.external.tsdb.model.EventItem;
import com.mopl.external.tsdb.model.EventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TsdbLeagueEventCollectService {

    private final TsdbClient tsdbClient;
    private final TsdbEventUpsertService upsertService;
    private final LeagueRepository leagueRepository;

    public void collectAll() {
        leagueRepository.findAll().forEach(league -> {
            collectPast(league.getLeagueId());
            collectNext(league.getLeagueId());
        });
    }

    private void collectPast(Long leagueId) {
        EventResponse response = tsdbClient.fetchPastLeagueEvent(leagueId);

        if (response == null || response.events() == null) {
            return;
        }

        response.events().forEach(item -> {
            if (!isValid(item)) {
                log.debug(
                    "Invalid TSDB past event skipped: leagueId={}, eventId={}, title={}, poster={}",
                    leagueId,
                    item.idEvent(),
                    item.strEvent(),
                    item.strThumb()
                );
                return;
            }

            try {
                upsertService.upsert(item);
                Thread.sleep(300);
            } catch (RuntimeException | InterruptedException e) {
                log.error(
                    "Failed to process past TSDB event: leagueId={}, eventId={}, reason={}",
                    leagueId,
                    item.idEvent(),
                    e.getMessage(),
                    e
                );
            }
        });
    }

    private void collectNext(Long leagueId) {
        EventResponse response = tsdbClient.fetchNextLeagueEvent(leagueId);

        if (response == null || response.events() == null) {
            return;
        }

        response.events().forEach(item -> {
            if (!isValid(item)) {
                log.debug(
                    "Invalid TSDB next event skipped: leagueId={}, eventId={}, title={}, poster={}",
                    leagueId,
                    item.idEvent(),
                    item.strEvent(),
                    item.strThumb()
                );
                return;
            }

            try {
                upsertService.upsert(item);
            } catch (RuntimeException e) {
                log.warn(
                    "Failed to process next TSDB event: leagueId={}, eventId={}, reason={}",
                    leagueId,
                    item.idEvent(),
                    e.getMessage()
                );
            }
        });
    }

    private boolean isValid(EventItem item) {
        if (item == null) {
            return false;
        }

        return item.idEvent() != null
            && item.strEvent() != null && !item.strEvent().isBlank()
            && item.strFilename() != null && !item.strFilename().isBlank()
            && item.strThumb() != null && !item.strThumb().isBlank();
    }
}
