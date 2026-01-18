package com.mopl.external.tmdb.model;

import java.util.List;

public record TmdbTvItem(
    Long id,
    String name,
    String overview,
    String poster_path,
    List<Integer> genre_ids
) {
}
