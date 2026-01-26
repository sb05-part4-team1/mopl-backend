package com.mopl.external.tmdb.exception;

import lombok.Getter;

@Getter
public class TmdbImageDownloadException extends RuntimeException {

    private final String posterPath;

    public TmdbImageDownloadException(String posterPath, Throwable cause) {
        super("TMDB image download failed: posterPath=" + posterPath, cause);
        this.posterPath = posterPath;
    }
}
