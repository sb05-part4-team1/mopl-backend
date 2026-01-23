package com.mopl.batch.collect.tmdb.support;

import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.exception.TmdbImageDownloadException;
import com.mopl.storage.provider.FileStorageProvider;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbPosterProcessor {

    private final TmdbClient tmdbClient;
    private final FileStorageProvider fileStorageProvider;

    public String uploadPosterIfPresent(ContentType type, Long externalId, String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }

        try (InputStream imageStream = tmdbClient.downloadImageStream(posterPath)) {
            if (imageStream == null) {
                return null;
            }

            String extension = extractExtension(posterPath);
            String filePath = buildFilePath(type, externalId, extension);

            return fileStorageProvider.upload(imageStream, filePath);

        } catch (TmdbImageDownloadException e) {
            log.warn(
                "TMDB poster download failed: type={}, externalId={}, path={}",
                type, externalId, e.getPosterPath()
            );
            return null;

        } catch (Exception e) {
            log.error(
                "Unexpected error while processing TMDB poster: type={}, externalId={}",
                type, externalId, e
            );
            return null;
        }
    }

    private String extractExtension(String posterPath) {
        int idx = posterPath.lastIndexOf('.');
        return (idx >= 0) ? posterPath.substring(idx) : "";
    }

    private String buildFilePath(ContentType type, Long externalId, String extension) {
        return "contents/tmdb/" + type.name() + "/" + externalId + extension;
    }
}
