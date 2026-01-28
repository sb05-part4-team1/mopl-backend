package com.mopl.batch.collect.tmdb.initializer;

import com.mopl.batch.collect.tmdb.service.genre.TmdbGenreSyncTxService;
import com.mopl.domain.repository.tag.GenreRepository;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmdbGenreInitializer implements CommandLineRunner {

    private final TmdbGenreSyncTxService syncService;
    private final GenreRepository genreRepository;

    @Override
    public void run(String... args) {

        if (genreRepository.count() == 0) {
            LogContext.with("initializer", "tmdbGenre").info("Genre table empty, initializing");
            syncService.syncAll();
        } else {
            LogContext.with("initializer", "tmdbGenre").info("Genre table already initialized, skipping");
        }
    }
}
