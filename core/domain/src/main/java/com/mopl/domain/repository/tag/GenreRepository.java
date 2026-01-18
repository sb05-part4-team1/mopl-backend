package com.mopl.domain.repository.tag;

import com.mopl.domain.model.tag.GenreModel;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GenreRepository {

    GenreModel save(GenreModel genreModel);

    Optional<GenreModel> findByTmdbId(Long tmdbId);

    List<GenreModel> findAllByTmdbIdIn(Collection<Long> tmdbIds);

    long count();
}
