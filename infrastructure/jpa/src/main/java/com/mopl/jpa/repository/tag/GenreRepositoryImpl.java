package com.mopl.jpa.repository.tag;

import com.mopl.domain.model.tag.GenreModel;
import com.mopl.domain.repository.tag.GenreRepository;
import com.mopl.jpa.entity.tag.GenreEntity;
import com.mopl.jpa.entity.tag.GenreEntityMapper;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreRepositoryImpl implements GenreRepository {

    private final JpaGenreRepository jpaGenreRepository;
    private final GenreEntityMapper genreEntityMapper;

    @Override
    public GenreModel save(GenreModel genreModel) {
        GenreEntity entity = genreEntityMapper.toEntity(genreModel);
        GenreEntity savedEntity = jpaGenreRepository.save(entity);
        return genreEntityMapper.toModel(savedEntity);
    }

    @Override
    public Optional<GenreModel> findByTmdbId(Long tmdbId) {
        return jpaGenreRepository.findByTmdbId(tmdbId)
            .map(genreEntityMapper::toModel);
    }

    @Override
    public List<GenreModel> findAllByTmdbIdIn(Collection<Long> tmdbIds) {
        if (tmdbIds == null || tmdbIds.isEmpty()) {
            return List.of();
        }

        return jpaGenreRepository.findAllByTmdbIdIn(tmdbIds).stream()
            .map(genreEntityMapper::toModel)
            .toList();
    }

    @Override
    public long count() {
        return jpaGenreRepository.count();
    }
}
