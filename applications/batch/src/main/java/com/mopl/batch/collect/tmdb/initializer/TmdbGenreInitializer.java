package com.mopl.batch.collect.tmdb.initializer;

import com.mopl.batch.collect.tmdb.service.genre.TmdbGenreSyncTxService;
import com.mopl.domain.repository.tag.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbGenreInitializer implements CommandLineRunner {

    private final TmdbGenreSyncTxService syncService;
    private final GenreRepository genreRepository;

    @Override
    public void run(String... args) {

        if (genreRepository.count() == 0) {
            log.info("Genre table empty. Initializing TMDB genres...");
            syncService.syncAll();
        } else {
            log.info("Genre table already initialized. Skip TMDB genre sync.");
        }
    }
}
