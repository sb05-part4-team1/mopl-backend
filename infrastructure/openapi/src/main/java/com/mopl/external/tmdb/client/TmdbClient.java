package com.mopl.external.tmdb.client;

import com.mopl.external.tmdb.model.TmdbGenreResponse;
import com.mopl.external.tmdb.model.TmdbMovieResponse;
import com.mopl.external.tmdb.model.TmdbTvResponse;
import com.mopl.external.tmdb.properteis.TmdbProperties;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TmdbClient {

    private final WebClient tmdbWebClient;
    private final WebClient tmdbImageClient;
    private final TmdbProperties props;

    public TmdbClient(
        @Qualifier("tmdbWebClient") WebClient tmdbWebClient,
        @Qualifier("tmdbImageClient") WebClient tmdbImageClient,
        TmdbProperties props
    ) {
        this.tmdbWebClient = tmdbWebClient;
        this.tmdbImageClient = tmdbImageClient;
        this.props = props;
    }

    public TmdbGenreResponse fetchMovieGenres() {
        return tmdbWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/genre/movie/list")
                .queryParam("language", "ko-KR")
                .build()
            )
            .retrieve()
            .bodyToMono(TmdbGenreResponse.class)
            .block();
    }

    public TmdbGenreResponse fetchTvGenres() {
        return tmdbWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/genre/tv/list")
                .queryParam("language", "ko-KR")
                .build()
            )
            .retrieve()
            .bodyToMono(TmdbGenreResponse.class)
            .block();
    }

    public TmdbMovieResponse fetchPopularMovies(int page) {
        return tmdbWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/movie/popular")
                .queryParam("language", "ko-KR")
                .queryParam("page", page)
                .build()
            )
            .retrieve()
            .bodyToMono(TmdbMovieResponse.class)
            .block();
    }

    public TmdbTvResponse fetchPopularTvSeries(int page) {
        return tmdbWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/tv/popular")
                .queryParam("language", "ko-KR")
                .queryParam("page", page)
                .build()
            )
            .retrieve()
            .bodyToMono(TmdbTvResponse.class)
            .block();
    }

    public InputStream downloadImageStream(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }

        Resource resource = tmdbImageClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/" + props.getImage().getDefaultSize() + posterPath)
                .build()
            )
            .retrieve()
            .bodyToMono(Resource.class)
            .block();

        try {
            return resource != null ? resource.getInputStream() : null;
        } catch (IOException e) {
            throw new RuntimeException("TMDB 이미지 다운로드 실패", e);
        }
    }
}
