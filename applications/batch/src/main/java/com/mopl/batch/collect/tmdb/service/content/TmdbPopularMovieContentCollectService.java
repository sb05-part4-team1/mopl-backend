package com.mopl.batch.collect.tmdb.service.content;

import com.mopl.batch.collect.tmdb.config.properties.TmdbCollectPolicyResolver;
import com.mopl.batch.collect.tmdb.config.properties.TmdbCollectProperties;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.model.TmdbMovieItem;
import com.mopl.external.tmdb.model.TmdbMovieResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
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
        int maxPage = policyResolver.maxPage(collectProperties.getMovieContent());
        List<ContentModel> inserted = new ArrayList<>();

        for (int page = 1; page <= maxPage; page++) {
            TmdbMovieResponse response = tmdbClient.fetchPopularMovies(page);

            if (response == null || response.results() == null) {
                log.debug(
                    "TMDB movie results empty: page={}",
                    page
                );
                continue;
            }

            for (TmdbMovieItem item : response.results()) {
                if (!isValid(item)) {
                    log.debug(
                        "TMDB invalid movie skipped: page={}, externalId={}",
                        page,
                        item == null ? null : item.id()
                    );
                    continue;
                }

                try {
                    ContentModel created = upsertService.upsertMovie(item);
                    if (created != null) {
                        inserted.add(created);
                    }

                } catch (DataIntegrityViolationException e) {
                    log.debug(
                        "TMDB duplicate skipped: externalId={}",
                        item.id()
                    );

                } catch (RuntimeException e) {
                    log.warn(
                        "Failed to process TMDB movie: title={}, reason={}",
                        item.title(),
                        e.getMessage()
                    );
                }
            }
        }

        afterCommitExecutor.execute(() -> contentSearchSyncPort.upsertAll(inserted));

        log.info(
            "TMDB movie collect done. inserted={}",
            inserted.size()
        );

        return inserted.size();
    }

    private boolean isValid(TmdbMovieItem item) {
        return item != null
            && item.title() != null && !item.title().isBlank()
            && item.poster_path() != null && !item.poster_path().isBlank()
            && item.overview() != null && !item.overview().isBlank();
    }
}
