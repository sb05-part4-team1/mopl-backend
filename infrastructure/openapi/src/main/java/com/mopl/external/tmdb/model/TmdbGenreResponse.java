package com.mopl.external.tmdb.model;

import java.util.List;

public record TmdbGenreResponse(
    List<TmdbGenreItem> genres
) {
}
