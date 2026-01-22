package com.mopl.batch.collect.tmdb.service.genre;

import com.mopl.domain.model.tag.GenreModel;
import com.mopl.domain.repository.tag.GenreRepository;
import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.model.TmdbGenreItem;
import com.mopl.external.tmdb.model.TmdbGenreResponse;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
            log.debug("TMDB genre response empty");
            return 0;
        }

        int inserted = 0;

        for (TmdbGenreItem item : response.genres()) {
            if (item == null || item.id() == null || item.name() == null || item.name().isBlank()) {
                log.debug("TMDB invalid genre skipped");
                continue;
            }

            Long tmdbId = item.id();

            if (!processedTmdbIds.add(tmdbId)) {
                log.debug("TMDB genre duplicated in response skipped: tmdbId={}, name={}",
                    tmdbId,
                    item.name());
                continue;
            }

            try {
                if (genreRepository.findByTmdbId(tmdbId).isEmpty()) {
                    genreRepository.save(GenreModel.create(tmdbId, item.name()));
                    inserted++;
                } else {
                    log.debug("TMDB genre already exists skipped: tmdbId={}, name={}",
                        tmdbId,
                        item.name());
                }

            } catch (DataIntegrityViolationException e) {
                log.debug("TMDB genre duplicate skipped: tmdbId={}, name={}",
                    tmdbId,
                    item.name());

            } catch (RuntimeException e) {
                log.warn("TMDB genre sync failed: tmdbId={}, name={}, reason={}",
                    tmdbId,
                    item.name(),
                    e.getMessage());
            }
        }

        return inserted;
    }
}
