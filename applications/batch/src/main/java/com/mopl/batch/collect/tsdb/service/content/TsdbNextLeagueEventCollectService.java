package com.mopl.batch.collect.tsdb.service.content;

import com.mopl.batch.collect.tsdb.config.TsdbCollectPolicyResolver;
import com.mopl.batch.collect.tsdb.config.TsdbCollectProperties;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.league.LeagueModel;
import com.mopl.domain.repository.league.LeagueRepository;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import com.mopl.external.tsdb.client.TsdbClient;
import com.mopl.external.tsdb.model.EventItem;
import com.mopl.external.tsdb.model.EventResponse;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TsdbNextLeagueEventCollectService {

    private final TsdbClient tsdbClient;
    private final TsdbLeagueEventUpsertTxService upsertService;
    private final LeagueRepository leagueRepository;
    private final TsdbCollectProperties collectProperties;
    private final TsdbCollectPolicyResolver policyResolver;

    private final AfterCommitExecutor afterCommitExecutor;
    private final ContentSearchSyncPort contentSearchSyncPort;

    public int collectNextLeagueEvents() {
        int sleepMs = policyResolver.sleepMs(collectProperties.leagueEvent());
        List<ContentModel> inserted = new ArrayList<>();

        for (LeagueModel league : leagueRepository.findAll()) {
            Long leagueId = league.getLeagueId();
            EventResponse response = tsdbClient.fetchNextLeagueEvent(leagueId);

            if (response == null || response.events() == null) {
                LogContext.with("service", "tsdbNextEventCollect")
                    .and("leagueId", leagueId)
                    .debug("Events empty");
                continue;
            }

            for (EventItem item : response.events()) {
                if (!isValid(item)) {
                    LogContext.with("service", "tsdbNextEventCollect")
                        .and("leagueId", leagueId)
                        .and("eventId", item == null ? null : item.idEvent())
                        .debug("Invalid event skipped");
                    continue;
                }

                try {
                    ContentModel created = upsertService.upsert(item);
                    if (created != null) {
                        inserted.add(created);
                    }

                } catch (DataIntegrityViolationException e) {
                    LogContext.with("service", "tsdbNextEventCollect")
                        .and("externalId", item.idEvent())
                        .debug("Duplicate skipped");

                } catch (RuntimeException e) {
                    LogContext.with("service", "tsdbNextEventCollect")
                        .and("eventId", item.idEvent())
                        .and("reason", e.getMessage())
                        .warn("Failed to process event");
                }

                sleepQuietly(sleepMs);
            }
        }

        afterCommitExecutor.execute(() -> contentSearchSyncPort.upsertAll(inserted));

        LogContext.with("service", "tsdbNextEventCollect")
            .and("inserted", inserted.size())
            .info("Collect completed");

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
