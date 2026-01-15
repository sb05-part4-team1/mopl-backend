package com.mopl.batch.service;

import com.mopl.domain.model.tag.GenreModel;
import com.mopl.domain.repository.tag.GenreRepository;
import com.mopl.external.client.TmdbClient;
import com.mopl.external.model.TmdbGenreItem;
import com.mopl.external.model.TmdbGenreResponse;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbGenreSyncService {

    private final TmdbClient tmdbClient;
    private final GenreRepository genreRepository;

    @Transactional
    public void syncAll() {
        Set<Long> processedTmdbIds = new HashSet<>();

        log.info("Start TMDB genre sync");

        sync(tmdbClient.fetchMovieGenres(), processedTmdbIds);
        sync(tmdbClient.fetchTvGenres(), processedTmdbIds);

        log.info("TMDB genre sync completed. totalProcessed={}", processedTmdbIds.size());
    }

    private void sync(TmdbGenreResponse response, Set<Long> processedTmdbIds) {
        if (response == null || response.genres() == null) {
            return;
        }

        for (TmdbGenreItem item : response.genres()) {
            Long tmdbId = item.id();

            if (!processedTmdbIds.add(tmdbId)) {
                continue;
            }

            try {
                if (genreRepository.findByTmdbId(tmdbId).isEmpty()) {
                    genreRepository.save(
                        GenreModel.create(tmdbId, item.name())
                    );
                }
            } catch (RuntimeException e) {
                log.debug(
                    "TMDB genre already exists or skipped: tmdbId={}, name={}",
                    tmdbId,
                    item.name()
                );
            }
        }
    }
}
