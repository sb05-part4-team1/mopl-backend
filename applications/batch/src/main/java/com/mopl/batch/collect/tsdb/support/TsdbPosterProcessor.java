package com.mopl.batch.collect.tsdb.support;

import com.mopl.external.tsdb.client.TsdbClient;
import com.mopl.external.tsdb.exception.TsdbImageDownloadException;
import com.mopl.storage.provider.FileStorageProvider;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TsdbPosterProcessor {

    private final TsdbClient tsdbClient;
    private final FileStorageProvider fileStorageProvider;

    public String uploadPosterIfPresent(Long externalId, String posterUrl, String sport) {
        if (posterUrl == null || posterUrl.isBlank()) {
            return null;
        }

        try (InputStream imageStream = tsdbClient.downloadImageStream(posterUrl)) {
            if (imageStream == null) {
                return null;
            }

            String extension = extractExtension(posterUrl);
            String filePath = buildFilePath(sport, externalId, extension);

            return fileStorageProvider.upload(imageStream, filePath);

        } catch (TsdbImageDownloadException e) {
            log.warn(
                "TSDB poster download failed: externalId={}, url={}",
                externalId, e.getImageUrl()
            );
            return null;

        } catch (Exception e) {
            log.error(
                "Unexpected error while processing TSDB poster: externalId={}",
                externalId, e
            );
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
