package com.mopl.jpa.repository.tag;

import com.mopl.jpa.entity.tag.GenreEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaGenreRepository extends JpaRepository<GenreEntity, UUID> {

    Optional<GenreEntity> findByTmdbId(Long tmdbId);

    List<GenreEntity> findAllByTmdbIdIn(Collection<Long> tmdbIds);
}
