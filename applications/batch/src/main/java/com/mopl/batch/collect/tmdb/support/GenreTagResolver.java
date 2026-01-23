package com.mopl.batch.collect.tmdb.support;

import com.mopl.domain.model.tag.GenreModel;
import com.mopl.domain.repository.tag.GenreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenreTagResolver {

    private final GenreRepository genreRepository;

    public List<String> resolve(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of();
        }

        List<Long> tmdbIds = genreIds.stream()
            .map(Long::valueOf)
            .toList();

        return genreRepository.findAllByTmdbIdIn(tmdbIds).stream()
            .map(GenreModel::getName)
            .distinct()
            .toList();
    }
}
