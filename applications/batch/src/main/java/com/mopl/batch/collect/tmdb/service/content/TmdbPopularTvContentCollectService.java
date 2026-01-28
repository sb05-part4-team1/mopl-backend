package com.mopl.batch.collect.tmdb.service.content;

import com.mopl.batch.collect.tmdb.config.TmdbCollectPolicyResolver;
import com.mopl.batch.collect.tmdb.config.TmdbCollectProperties;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.model.TmdbTvItem;
import com.mopl.external.tmdb.model.TmdbTvResponse;
import com.mopl.logging.context.LogContext;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmdbPopularTvContentCollectService {

    private final TmdbClient tmdbClient;
    private final TmdbPopularContentUpsertTxService upsertService;
    private final TmdbCollectProperties collectProperties;
    private final TmdbCollectPolicyResolver policyResolver;

    private final AfterCommitExecutor afterCommitExecutor;
    private final ContentSearchSyncPort contentSearchSyncPort;

    public int collectPopularTvSeries() {
        int maxPage = policyResolver.maxPage(collectProperties.tvContent());
        List<ContentModel> inserted = new ArrayList<>();

        for (int page = 1; page <= maxPage; page++) {
            TmdbTvResponse response = tmdbClient.fetchPopularTvSeries(page);

            if (response == null || response.results() == null) {
                LogContext.with("service", "tmdbTvCollect")
                    .and("page", page)
                    .debug("Results empty");
                continue;
            }

            for (TmdbTvItem item : response.results()) {
                if (!isValid(item)) {
                    LogContext.with("service", "tmdbTvCollect")
                        .and("page", page)
                        .and("externalId", item == null ? null : item.id())
                        .debug("Invalid tv skipped");
                    continue;
                }

                try {
                    ContentModel created = upsertService.upsertTv(item);
                    if (created != null) {
                        inserted.add(created);
                    }

                } catch (DataIntegrityViolationException e) {
                    LogContext.with("service", "tmdbTvCollect")
                        .and("externalId", item.id())
                        .debug("Duplicate skipped");

                } catch (RuntimeException e) {
                    LogContext.with("service", "tmdbTvCollect")
                        .and("name", item.name())
                        .and("reason", e.getMessage())
                        .warn("Failed to process tv");
                }
            }
        }

        afterCommitExecutor.execute(() -> contentSearchSyncPort.upsertAll(inserted));

        LogContext.with("service", "tmdbTvCollect")
            .and("inserted", inserted.size())
            .info("Collect completed");

        return inserted.size();
    }

    private boolean isValid(TmdbTvItem item) {
        return item != null
            && item.name() != null && !item.name().isBlank()
            && item.poster_path() != null && !item.poster_path().isBlank()
            && item.overview() != null && !item.overview().isBlank();
    }
}
