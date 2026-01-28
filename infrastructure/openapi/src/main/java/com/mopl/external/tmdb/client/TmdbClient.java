package com.mopl.external.tmdb.client;

import com.mopl.external.ExternalApiMetrics;
import com.mopl.external.tmdb.exception.TmdbImageDownloadException;
import com.mopl.external.tmdb.model.TmdbGenreResponse;
import com.mopl.external.tmdb.model.TmdbMovieResponse;
import com.mopl.external.tmdb.model.TmdbTvResponse;
import com.mopl.external.tmdb.properties.TmdbProperties;
import com.mopl.logging.context.LogContext;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TmdbClient {

    private static final String API_NAME = "tmdb";

    private final WebClient tmdbWebClient;
    private final WebClient tmdbImageClient;
    private final TmdbProperties props;
    private final ExternalApiMetrics metrics;

    public TmdbClient(
        @Qualifier("tmdbWebClient") WebClient tmdbWebClient,
        @Qualifier("tmdbImageClient") WebClient tmdbImageClient,
        TmdbProperties props,
        @Nullable ExternalApiMetrics metrics
    ) {
        this.tmdbWebClient = tmdbWebClient;
        this.tmdbImageClient = tmdbImageClient;
        this.props = props;
        this.metrics = metrics;
    }

    public TmdbGenreResponse fetchMovieGenres() {
        String endpoint = "movieGenres";
        LogContext.with("api", API_NAME).and("endpoint", endpoint).debug("Fetching");
        Timer.Sample sample = startTimer();
        try {
            TmdbGenreResponse response = tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/genre/movie/list")
                    .queryParam("language", "ko-KR")
                    .build()
                )
                .retrieve()
                .bodyToMono(TmdbGenreResponse.class)
                .block();
            recordSuccess(sample, endpoint);
            return response;
        } catch (Exception e) {
            recordError(sample, endpoint, e.getClass().getSimpleName());
            throw e;
        }
    }

    public TmdbGenreResponse fetchTvGenres() {
        String endpoint = "tvGenres";
        LogContext.with("api", API_NAME).and("endpoint", endpoint).debug("Fetching");
        Timer.Sample sample = startTimer();
        try {
            TmdbGenreResponse response = tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/genre/tv/list")
                    .queryParam("language", "ko-KR")
                    .build()
                )
                .retrieve()
                .bodyToMono(TmdbGenreResponse.class)
                .block();
            recordSuccess(sample, endpoint);
            return response;
        } catch (Exception e) {
            recordError(sample, endpoint, e.getClass().getSimpleName());
            throw e;
        }
    }

    public TmdbMovieResponse fetchPopularMovies(int page) {
        String endpoint = "popularMovies";
        LogContext.with("api", API_NAME).and("endpoint", endpoint).and("page", page).debug("Fetching");
        Timer.Sample sample = startTimer();
        try {
            TmdbMovieResponse response = tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/movie/popular")
                    .queryParam("language", "ko-KR")
                    .queryParam("page", page)
                    .build()
                )
                .retrieve()
                .bodyToMono(TmdbMovieResponse.class)
                .block();
            recordSuccess(sample, endpoint);
            return response;
        } catch (Exception e) {
            recordError(sample, endpoint, e.getClass().getSimpleName());
            throw e;
        }
    }

    public TmdbTvResponse fetchPopularTvSeries(int page) {
        String endpoint = "popularTv";
        LogContext.with("api", API_NAME).and("endpoint", endpoint).and("page", page).debug("Fetching");
        Timer.Sample sample = startTimer();
        try {
            TmdbTvResponse response = tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/tv/popular")
                    .queryParam("language", "ko-KR")
                    .queryParam("page", page)
                    .build()
                )
                .retrieve()
                .bodyToMono(TmdbTvResponse.class)
                .block();
            recordSuccess(sample, endpoint);
            return response;
        } catch (Exception e) {
            recordError(sample, endpoint, e.getClass().getSimpleName());
            throw e;
        }
    }

    public Resource downloadImage(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }

        Timer.Sample sample = startTimer();
        try {
            Resource resource = tmdbImageClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/" + props.getImage().getDefaultSize() + posterPath)
                    .build()
                )
                .retrieve()
                .bodyToMono(Resource.class)
                .block();
            recordSuccess(sample, "downloadImage");
            recordImageDownload(resource);
            return resource;
        } catch (RuntimeException e) {
            recordError(sample, "downloadImage", e.getClass().getSimpleName());
            throw new TmdbImageDownloadException(posterPath, e);
        }
    }

    private Timer.Sample startTimer() {
        return metrics != null ? metrics.startTimer() : null;
    }

    private void recordSuccess(Timer.Sample sample, String endpoint) {
        if (metrics != null && sample != null) {
            metrics.recordSuccess(sample, API_NAME, endpoint);
        }
    }

    private void recordError(Timer.Sample sample, String endpoint, String errorType) {
        if (metrics != null && sample != null) {
            metrics.recordError(sample, API_NAME, endpoint, errorType);
        }
    }

    private void recordImageDownload(Resource resource) {
        if (metrics != null && resource != null) {
            try {
                metrics.recordImageDownload(API_NAME, resource.contentLength());
            } catch (Exception ignored) {
            }
        }
    }
}
