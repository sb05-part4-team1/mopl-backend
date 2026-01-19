package com.mopl.batch.tsdb.service;

import com.mopl.domain.model.content.ContentExternalProvider;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.repository.content.ContentExternalMappingRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.external.tsdb.client.TsdbClient;
import com.mopl.external.tsdb.exception.TsdbImageDownloadException;
import com.mopl.external.tsdb.model.EventItem;
import com.mopl.storage.provider.FileStorageProvider;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TsdbEventUpsertService {

    private final ContentService contentService;
    private final ContentExternalMappingRepository externalMappingRepository;
    private final TsdbClient tsdbClient;
    private final FileStorageProvider fileStorageProvider;

    @Transactional
    public void upsert(EventItem item) {

        Long externalId = item.idEvent();

        if (externalMappingRepository.exists(ContentExternalProvider.TSDB, externalId)) {
            return;
        }

        String thumbnailUrl = uploadPosterIfExists(item);
        List<String> tagNames = buildTags(item);

        ContentModel content = contentService.create(
            ContentModel.create(
                ContentType.sport,
                item.strEvent().strip(),
                item.strFilename().strip(),
                thumbnailUrl
            ),
            tagNames
        );

        externalMappingRepository.save(
            ContentExternalProvider.TSDB,
            externalId,
            content.getId()
        );
    }

    private String uploadPosterIfExists(EventItem item) {
        if (item.strThumb() == null || item.strThumb().isBlank()) {
            return null;
        }

        try (InputStream imageStream = tsdbClient.downloadImageStream(item.strThumb())) {

            if (imageStream == null) {
                return null;
            }

            String extension = item.strThumb().substring(item.strThumb().lastIndexOf("."));
            String filePath =
                "contents/tsdb/" + item.strSport() + "/" + item.idEvent() + extension;

            String storedPath = fileStorageProvider.upload(imageStream, filePath);
            return fileStorageProvider.getUrl(storedPath);

        } catch (TsdbImageDownloadException e) {
            log.warn(
                "TSDB poster download failed: eventId={}, url={}",
                item.idEvent(),
                e.getImageUrl()
            );
            return null;

        } catch (Exception e) {
            log.error(
                "Unexpected error while processing TSDB poster: eventId={}",
                item.idEvent(),
                e
            );
            return null;
        }
    }

    private List<String> buildTags(EventItem item) {
        return Stream.of(
                item.strSport(),
                item.strHomeTeam(),
                item.strAwayTeam()
            )
            .filter(Objects::nonNull)
            .map(String::strip)
            .filter(s -> !s.isBlank())
            .distinct()
            .toList();
    }
}
