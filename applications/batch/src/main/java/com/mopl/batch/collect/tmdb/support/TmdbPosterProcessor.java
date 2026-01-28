package com.mopl.batch.collect.tmdb.support;

import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.exception.TmdbImageDownloadException;
import com.mopl.logging.context.LogContext;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TmdbPosterProcessor {

    private final TmdbClient tmdbClient;
    private final StorageProvider storageProvider;

    public String uploadPosterIfPresent(ContentType type, Long externalId, String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }

        try {
            Resource resource = tmdbClient.downloadImage(posterPath);
            if (resource == null) {
                return null;
            }

            String extension = extractExtension(posterPath);
            String filePath = buildFilePath(type, externalId, extension);

            storageProvider.upload(resource.getInputStream(), resource.contentLength(), filePath);
            return filePath;
        } catch (TmdbImageDownloadException e) {
            LogContext.with("processor", "tmdbPoster")
                .and("type", type)
                .and("externalId", externalId)
                .and("path", e.getPosterPath())
                .warn("Poster download failed");

        } catch (IOException e) {
            LogContext.with("processor", "tmdbPoster")
                .and("type", type)
                .and("externalId", externalId)
                .error("Failed to read image stream", e);

        } catch (Exception e) {
            LogContext.with("processor", "tmdbPoster")
                .and("type", type)
                .and("externalId", externalId)
                .error("Unexpected error while processing poster", e);
        }
        return null;
    }

    private String extractExtension(String posterPath) {
        int idx = posterPath.lastIndexOf('.');
        return (idx >= 0) ? posterPath.substring(idx) : "";
    }

    private String buildFilePath(ContentType type, Long externalId, String extension) {
        return "contents/tmdb/" + type.name() + "/" + externalId + extension;
    }
}
