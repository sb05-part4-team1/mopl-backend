package com.mopl.batch.tmdb.service;

import com.mopl.domain.model.content.ContentExternalProvider;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.model.tag.GenreModel;
import com.mopl.domain.repository.content.ContentExternalMappingRepository;
import com.mopl.domain.repository.tag.GenreRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.model.TmdbMovieItem;
import com.mopl.external.tmdb.model.TmdbTvItem;
import com.mopl.storage.provider.FileStorageProvider;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbPopularContentUpsertService {

    private final ContentService contentService;
    private final ContentExternalMappingRepository contentExternalMappingRepository;
    private final GenreRepository genreRepository;
    private final TmdbClient tmdbClient;
    private final FileStorageProvider fileStorageProvider;

    @Transactional
    public void upsertMovie(TmdbMovieItem item) {
        upsert(
            ContentType.movie,
            item.id(),
            item.title(),
            item.overview(),
            item.poster_path(),
            item.genre_ids()
        );
    }

    @Transactional
    public void upsertTv(TmdbTvItem item) {
        upsert(
            ContentType.tvSeries,
            item.id(),
            item.name(),
            item.overview(),
            item.poster_path(),
            item.genre_ids()
        );
    }

    public void upsert(
        ContentType type,
        Long externalId,
        String title,
        String overview,
        String posterPath,
        List<Integer> genreIds
    ) {
        if (contentExternalMappingRepository.exists(ContentExternalProvider.TMDB, externalId)) {
            return;
        }

        String thumbnailUrl = processPosterImage(type, externalId, posterPath);
        List<String> tagNames = resolveGenreTags(genreIds);

        ContentModel content = contentService.create(
            ContentModel.create(type, title, overview, thumbnailUrl),
            tagNames
        );

        contentExternalMappingRepository.save(
            ContentExternalProvider.TMDB,
            externalId,
            content.getId()
        );
    }

    private String processPosterImage(
        ContentType type,
        Long externalId,
        String posterPath
    ) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }

        try (InputStream imageStream = tmdbClient.downloadImageStream(posterPath)) {

            if (imageStream == null) {
                return null;
            }

            String extension = posterPath.substring(posterPath.lastIndexOf("."));
            String filePath = "contents/tmdb/" + type.name() + "/" + externalId + extension;
            String storedPath = fileStorageProvider.upload(imageStream, filePath);

            return fileStorageProvider.getUrl(storedPath);

        } catch (Exception e) {
            log.warn(
                "Failed to download poster: type={}, externalId={}, path={}",
                type, externalId, posterPath
            );
            return null;
        }
    }

    private List<String> resolveGenreTags(List<Integer> genreIds) {
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
