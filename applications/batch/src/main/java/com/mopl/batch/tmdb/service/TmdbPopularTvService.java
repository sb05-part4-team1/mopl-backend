package com.mopl.batch.tmdb.service;

import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.model.TmdbTvItem;
import com.mopl.external.tmdb.model.TmdbTvResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbPopularTvService {

    private static final int MAX_PAGE = 20; // TMDB rate limit 고려

    private final TmdbClient tmdbClient;
    private final TmdbPopularContentUpsertService upsertService;

    public void collectPopularTvSeries() {
        for (int page = 1; page <= MAX_PAGE; page++) {
            TmdbTvResponse response = tmdbClient.fetchPopularTvSeries(page);

            response.results().forEach(item -> {
                if (!isValid(item)) {
                    log.debug(
                        "Invalid TMDB tv data: name={}, poster={}, overviewLength={}",
                        item.name(),
                        item.poster_path(),
                        item.overview() == null ? null : item.overview().length()
                    );
                    return;
                }

                try {
                    upsertService.upsertTv(item);
                } catch (DataIntegrityViolationException e) {
                    log.debug("TMDB duplicate skipped: externalId={}", item.id());
                } catch (RuntimeException e) {
                    log.warn(
                        "Failed to process TMDB tv: name={}, reason={}",
                        item.name(),
                        e.getMessage()
                    );
                }
            });
        }
    }

    private boolean isValid(TmdbTvItem item) {
        return item.name() != null && !item.name().isBlank()
            && item.poster_path() != null && !item.poster_path().isBlank()
            && item.overview() != null && !item.overview().isBlank();
    }
}
