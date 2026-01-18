package com.mopl.batch.tmdb.job;

import com.mopl.batch.tmdb.service.TmdbPopularMovieService;
import com.mopl.batch.tmdb.service.TmdbPopularTvService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmdbPopularContentJob {

    private final TmdbPopularMovieService movieService;
    private final TmdbPopularTvService tvService;

    public void run() {
        movieService.collectPopularMovies();
        tvService.collectPopularTvSeries();
    }
}
