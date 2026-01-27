package com.mopl.batch.collect.tmdb.service.content;

import com.mopl.batch.collect.tmdb.config.TmdbCollectPolicyResolver;
import com.mopl.batch.collect.tmdb.config.TmdbCollectProperties;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.model.TmdbMovieItem;
import com.mopl.external.tmdb.model.TmdbMovieResponse;
import com.mopl.logging.context.LogContext;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmdbPopularMovieContentCollectService {

    private final TmdbClient tmdbClient;
    private final TmdbPopularContentUpsertTxService upsertService;
    private final TmdbCollectProperties collectProperties;
    private final TmdbCollectPolicyResolver policyResolver;

    private final AfterCommitExecutor afterCommitExecutor;
    private final ContentSearchSyncPort contentSearchSyncPort;

    public int collectPopularMovies() {
        int maxPage = policyResolver.maxPage(collectProperties.movieContent());
        List<ContentModel> inserted = new ArrayList<>();

        for (int page = 1; page <= maxPage; page++) {
            TmdbMovieResponse response = tmdbClient.fetchPopularMovies(page);

            if (response == null || response.results() == null) {
                LogContext.with("service", "tmdbMovieCollect")
                    .and("page", page)
                    .debug("Results empty");
                continue;
            }

            for (TmdbMovieItem item : response.results()) {
                if (!isValid(item)) {
                    LogContext.with("service", "tmdbMovieCollect")
                        .and("page", page)
                        .and("externalId", item == null ? null : item.id())
                        .debug("Invalid movie skipped");
                    continue;
                }

                try {
                    ContentModel created = upsertService.upsertMovie(item);
                    if (created != null) {
                        inserted.add(created);
                    }

                } catch (DataIntegrityViolationException e) {
                    LogContext.with("service", "tmdbMovieCollect")
                        .and("externalId", item.id())
                        .debug("Duplicate skipped");

                } catch (RuntimeException e) {
                    LogContext.with("service", "tmdbMovieCollect")
                        .and("title", item.title())
                        .and("reason", e.getMessage())
                        .warn("Failed to process movie");
                }
            }
        }

        afterCommitExecutor.execute(() -> contentSearchSyncPort.upsertAll(inserted));

        LogContext.with("service", "tmdbMovieCollect")
            .and("inserted", inserted.size())
            .info("Collect completed");

        return inserted.size();
    }

    private boolean isValid(TmdbMovieItem item) {
        return item != null
            && item.title() != null && !item.title().isBlank()
            && item.poster_path() != null && !item.poster_path().isBlank()
            && item.overview() != null && !item.overview().isBlank();
    }
}
