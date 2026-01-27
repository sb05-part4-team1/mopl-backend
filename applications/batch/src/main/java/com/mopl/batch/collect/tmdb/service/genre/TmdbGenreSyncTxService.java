package com.mopl.batch.collect.tmdb.service.genre;

import com.mopl.domain.model.tag.GenreModel;
import com.mopl.domain.repository.tag.GenreRepository;
import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.model.TmdbGenreItem;
import com.mopl.external.tmdb.model.TmdbGenreResponse;
import com.mopl.logging.context.LogContext;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmdbGenreSyncTxService {

    private final TmdbClient tmdbClient;
    private final GenreRepository genreRepository;

    @Transactional
    public int syncAll() {
        Set<Long> processedTmdbIds = new HashSet<>();

        int inserted = 0;

        inserted += sync(tmdbClient.fetchMovieGenres(), processedTmdbIds);
        inserted += sync(tmdbClient.fetchTvGenres(), processedTmdbIds);

        return inserted;
    }

    private int sync(TmdbGenreResponse response, Set<Long> processedTmdbIds) {
        if (response == null || response.genres() == null) {
            LogContext.with("service", "tmdbGenreSync").debug("Genre response empty");
            return 0;
        }

        int inserted = 0;

        for (TmdbGenreItem item : response.genres()) {
            if (item == null || item.id() == null || item.name() == null || item.name().isBlank()) {
                LogContext.with("service", "tmdbGenreSync").debug("Invalid genre skipped");
                continue;
            }

            Long tmdbId = item.id();

            if (!processedTmdbIds.add(tmdbId)) {
                LogContext.with("service", "tmdbGenreSync")
                    .and("tmdbId", tmdbId)
                    .and("name", item.name())
                    .debug("Duplicated in response skipped");
                continue;
            }

            try {
                if (genreRepository.findByTmdbId(tmdbId).isEmpty()) {
                    genreRepository.save(GenreModel.create(tmdbId, item.name()));
                    inserted++;
                } else {
                    LogContext.with("service", "tmdbGenreSync")
                        .and("tmdbId", tmdbId)
                        .and("name", item.name())
                        .debug("Already exists skipped");
                }

            } catch (DataIntegrityViolationException e) {
                LogContext.with("service", "tmdbGenreSync")
                    .and("tmdbId", tmdbId)
                    .and("name", item.name())
                    .debug("Duplicate skipped");

            } catch (RuntimeException e) {
                LogContext.with("service", "tmdbGenreSync")
                    .and("tmdbId", tmdbId)
                    .and("name", item.name())
                    .and("reason", e.getMessage())
                    .warn("Sync failed");
            }
        }

        return inserted;
    }
}
