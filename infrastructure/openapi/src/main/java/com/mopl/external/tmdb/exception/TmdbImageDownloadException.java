package com.mopl.external.tmdb.exception;

public class TmdbImageDownloadException extends RuntimeException {

    private final String posterPath;

    public TmdbImageDownloadException(String posterPath, Throwable cause) {
        super("TMDB image download failed: posterPath=" + posterPath, cause);
        this.posterPath = posterPath;
    }

    public String getPosterPath() {
        return posterPath;
    }
}
