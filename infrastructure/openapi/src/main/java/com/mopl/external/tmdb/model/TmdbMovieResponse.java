package com.mopl.external.tmdb.model;

import java.util.List;

public record TmdbMovieResponse(
    List<TmdbMovieItem> results
) {
}
