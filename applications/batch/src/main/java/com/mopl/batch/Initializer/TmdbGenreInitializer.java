package com.mopl.batch.Initializer;

import com.mopl.batch.service.TmdbGenreSyncService;
import com.mopl.domain.repository.tag.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbGenreInitializer implements CommandLineRunner {

    private final TmdbGenreSyncService syncService;
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
