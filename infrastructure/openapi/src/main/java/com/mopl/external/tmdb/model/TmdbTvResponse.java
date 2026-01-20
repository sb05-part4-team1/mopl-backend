package com.mopl.external.tmdb.model;

import java.util.List;

public record TmdbTvResponse(
    List<TmdbTvItem> results
) {
}
