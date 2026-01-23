package com.mopl.batch.collect.tmdb.service.content;

import com.mopl.batch.collect.tmdb.support.GenreTagResolver;
import com.mopl.batch.collect.tmdb.support.TmdbPosterProcessor;
import com.mopl.domain.model.content.ContentExternalProvider;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.repository.content.ContentExternalMappingRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.external.tmdb.model.TmdbMovieItem;
import com.mopl.external.tmdb.model.TmdbTvItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmdbPopularContentUpsertTxService {

    private final ContentService contentService;
    private final ContentExternalMappingRepository contentExternalMappingRepository;
    private final TmdbPosterProcessor tmdbPosterProcessor;
    private final GenreTagResolver genreTagResolver;

    @Transactional
    public boolean upsertMovie(TmdbMovieItem item) {
        return upsert(
            ContentType.movie,
            item.id(),
            item.title(),
            item.overview(),
            item.poster_path(),
            item.genre_ids()
        );
    }

    @Transactional
    public boolean upsertTv(TmdbTvItem item) {
        return upsert(
            ContentType.tvSeries,
            item.id(),
            item.name(),
            item.overview(),
            item.poster_path(),
            item.genre_ids()
        );
    }

    public boolean upsert(
        ContentType type,
        Long externalId,
        String title,
        String overview,
        String posterPath,
        List<Integer> genreIds
    ) {
        if (contentExternalMappingRepository.exists(ContentExternalProvider.TMDB, externalId)) {
            return false;
        }

        String thumbnailUrl = tmdbPosterProcessor.uploadPosterIfPresent(type, externalId,
            posterPath);
        List<String> tagNames = genreTagResolver.resolve(genreIds);

        ContentModel content = contentService.create(
            ContentModel.create(type, title, overview, thumbnailUrl),
            tagNames
        );

        contentExternalMappingRepository.save(
            ContentExternalProvider.TMDB,
            externalId,
            content.getId()
        );

        return true;
    }
}
