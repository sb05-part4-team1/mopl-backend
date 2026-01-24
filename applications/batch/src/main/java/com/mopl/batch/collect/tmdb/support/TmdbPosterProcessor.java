package com.mopl.batch.collect.tmdb.support;

import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.exception.TmdbImageDownloadException;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
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
            log.warn(
                "TMDB poster download failed: type={}, externalId={}, path={}",
                type, externalId, e.getPosterPath()
            );

        } catch (IOException e) {
            log.error(
                "Failed to read image stream: type={}, externalId={}",
                type, externalId, e
            );

        } catch (Exception e) {
            log.error(
                "Unexpected error while processing TMDB poster: type={}, externalId={}",
                type, externalId, e
            );
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
