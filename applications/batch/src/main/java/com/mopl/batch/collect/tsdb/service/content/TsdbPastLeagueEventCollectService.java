package com.mopl.batch.collect.tsdb.service.content;

import com.mopl.batch.collect.tsdb.properties.TsdbCollectPolicyResolver;
import com.mopl.batch.collect.tsdb.properties.TsdbCollectProperties;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.league.LeagueRepository;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import com.mopl.external.tsdb.client.TsdbClient;
import com.mopl.external.tsdb.model.EventItem;
import com.mopl.external.tsdb.model.EventResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TsdbPastLeagueEventCollectService {

    private final TsdbClient tsdbClient;
    private final TsdbLeagueEventUpsertTxService upsertService;
    private final LeagueRepository leagueRepository;
    private final TsdbCollectProperties collectProperties;
    private final TsdbCollectPolicyResolver policyResolver;

    private final AfterCommitExecutor afterCommitExecutor;
    private final ContentSearchSyncPort contentSearchSyncPort;

    public int collectPastLeagueEvents() {
        int sleepMs = policyResolver.sleepMs(collectProperties.getLeagueEvent());
        List<ContentModel> inserted = new ArrayList<>();

        for (var league : leagueRepository.findAll()) {
            Long leagueId = league.getLeagueId();
            EventResponse response = tsdbClient.fetchPastLeagueEvent(leagueId);

            if (response == null || response.events() == null) {
                log.debug(
                    "TSDB past events empty: leagueId={}",
                    leagueId
                );
                continue;
            }

            for (EventItem item : response.events()) {
                if (!isValid(item)) {
                    log.debug(
                        "TSDB invalid past event skipped: leagueId={}, eventId={}",
                        leagueId,
                        item == null ? null : item.idEvent()
                    );
                    continue;
                }

                try {
                    ContentModel created = upsertService.upsert(item);
                    if (created != null) {
                        inserted.add(created);
                    }

                } catch (DataIntegrityViolationException e) {
                    log.debug(
                        "TSDB duplicate skipped: externalId={}",
                        item.idEvent()
                    );

                } catch (RuntimeException e) {
                    log.warn(
                        "Failed to process TSDB past event: eventId={}, reason={}",
                        item.idEvent(),
                        e.getMessage()
                    );
                }

                sleepQuietly(sleepMs);
            }
        }

        afterCommitExecutor.execute(() -> contentSearchSyncPort.upsertAll(inserted)
        );

        log.info(
            "TSDB past league events collect done. inserted={}",
            inserted.size()
        );

        return inserted.size();
    }

    private boolean isValid(EventItem item) {
        return item != null
            && item.idEvent() != null
            && item.strEvent() != null && !item.strEvent().isBlank()
            && item.strFilename() != null && !item.strFilename().isBlank()
            && item.strThumb() != null && !item.strThumb().isBlank();
    }

    private void sleepQuietly(int millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
