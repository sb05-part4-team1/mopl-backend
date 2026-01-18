package com.mopl.external.tmdb.model;

import java.util.List;

public record TmdbMovieItem(
    Long id,
    String title,
    String overview,
    String poster_path,
    List<Integer> genre_ids
) {
}
