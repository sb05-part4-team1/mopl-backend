package com.mopl.batch.tmdb.service;

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
public class TmdbPopularMovieService {

    private static final int MAX_PAGE = 20; // TMDB rate limit 고려

    private final TmdbClient tmdbClient;
    private final TmdbPopularContentUpsertService upsertService;

    public void collectPopularMovies() {
        for (int page = 1; page <= MAX_PAGE; page++) {
            TmdbMovieResponse response = tmdbClient.fetchPopularMovies(page);

            response.results().forEach(item -> {
                if (!isValid(item)) {
                    log.debug(
                        "Invalid TMDB movie data: title={}, poster={}, overviewLength={}",
                        item.title(),
                        item.poster_path(),
                        item.overview() == null ? null : item.overview().length()
                    );
                    return;
                }

                try {
                    upsertService.upsertMovie(item);
                } catch (DataIntegrityViolationException e) {
                    log.debug("TMDB duplicate skipped: externalId={}", item.id());
                } catch (RuntimeException e) {
                    log.warn(
                        "Failed to process TMDB movie: title={}, reason={}",
                        item.title(),
                        e.getMessage()
                    );
                }
            });
        }
    }

    private boolean isValid(TmdbMovieItem item) {
        return item.title() != null && !item.title().isBlank()
            && item.poster_path() != null && !item.poster_path().isBlank()
            && item.overview() != null && !item.overview().isBlank();
    }
}
