package com.mopl.batch.collect.tsdb.support;

import com.mopl.external.tsdb.client.TsdbClient;
import com.mopl.external.tsdb.exception.TsdbImageDownloadException;
import com.mopl.logging.context.LogContext;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TsdbPosterProcessor {

    private final TsdbClient tsdbClient;
    private final StorageProvider storageProvider;

    public String uploadPosterIfPresent(Long externalId, String posterUrl, String sport) {
        if (posterUrl == null || posterUrl.isBlank()) {
            return null;
        }

        try {
            Resource resource = tsdbClient.downloadImage(posterUrl);
            if (resource == null) {
                return null;
            }

            String extension = extractExtension(posterUrl);
            String filePath = buildFilePath(sport, externalId, extension);

            storageProvider.upload(resource.getInputStream(), resource.contentLength(), filePath);
            return filePath;

        } catch (TsdbImageDownloadException e) {
            LogContext.with("processor", "tsdbPoster")
                .and("externalId", externalId)
                .and("url", e.getImageUrl())
                .warn("Poster download failed");
            return null;

        } catch (IOException e) {
            LogContext.with("processor", "tsdbPoster")
                .and("externalId", externalId)
                .error("Failed to read image stream", e);
            return null;

        } catch (Exception e) {
            LogContext.with("processor", "tsdbPoster")
                .and("externalId", externalId)
                .error("Unexpected error while processing poster", e);
            return null;
        }
    }

    private String extractExtension(String posterUrl) {
        int idx = posterUrl.lastIndexOf('.');
        return (idx >= 0) ? posterUrl.substring(idx) : "";
    }

    private String buildFilePath(String sport, Long externalId, String extension) {
        return "contents/tsdb/" + sport + "/" + externalId + extension;
    }
}
