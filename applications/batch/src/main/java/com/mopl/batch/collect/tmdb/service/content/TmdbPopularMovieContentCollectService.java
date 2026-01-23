package com.mopl.batch.collect.tmdb.service.content;

import com.mopl.batch.collect.tmdb.properties.TmdbCollectPolicyResolver;
import com.mopl.batch.collect.tmdb.properties.TmdbCollectProperties;
import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.model.TmdbMovieItem;
import com.mopl.external.tmdb.model.TmdbMovieResponse;
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

    public int collectPopularMovies() {
        int maxPage = policyResolver.maxPage(collectProperties.getMovieContent());
        int processed = 0;

        for (int page = 1; page <= maxPage; page++) {
            TmdbMovieResponse response = tmdbClient.fetchPopularMovies(page);

            if (response == null || response.results() == null) {
                log.debug("TMDB movie results empty: page={}",
                    page);
                continue;
            }

            for (TmdbMovieItem item : response.results()) {
                if (!isValid(item)) {
                    log.debug("TMDB invalid movie skipped: page={}, externalId={}",
                        page,
                        item == null ? null : item.id());
                    continue;
                }

                try {
                    boolean created = upsertService.upsertMovie(item);
                    if (created) {
                        processed++;
                    }

                } catch (DataIntegrityViolationException e) {
                    log.debug("TMDB duplicate skipped: externalId={}",
                        item.id());

                } catch (RuntimeException e) {
                    log.warn("Failed to process TMDB movie: title={}, reason={}",
                        item.title(),
                        e.getMessage());
                }
            }
        }

        return processed;
    }

    private boolean isValid(TmdbMovieItem item) {
        return item != null
            && item.title() != null && !item.title().isBlank()
            && item.poster_path() != null && !item.poster_path().isBlank()
            && item.overview() != null && !item.overview().isBlank();
    }
}
