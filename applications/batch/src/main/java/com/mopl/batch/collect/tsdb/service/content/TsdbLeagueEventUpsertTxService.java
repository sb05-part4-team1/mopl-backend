package com.mopl.batch.collect.tsdb.service.content;

import com.mopl.batch.collect.tsdb.support.TsdbEventTagResolver;
import com.mopl.batch.collect.tsdb.support.TsdbPosterProcessor;
import com.mopl.domain.model.content.ContentExternalProvider;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.repository.content.ContentExternalMappingRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.content.ContentTagService;
import com.mopl.external.tsdb.model.EventItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TsdbLeagueEventUpsertTxService {

    private final ContentService contentService;
    private final ContentTagService contentTagService;
    private final ContentExternalMappingRepository externalMappingRepository;
    private final TsdbPosterProcessor tsdbPosterProcessor;
    private final TsdbEventTagResolver tsdbEventTagResolver;

    @Transactional
    public boolean upsert(EventItem item) {
        Long externalId = item.idEvent();

        if (externalMappingRepository.exists(ContentExternalProvider.TSDB, externalId)) {
            return false;
        }

        String thumbnailPath = tsdbPosterProcessor.uploadPosterIfPresent(
            externalId,
            item.strThumb(),
            item.strSport()
        );

        List<String> tagNames = tsdbEventTagResolver.resolve(item);

        ContentModel content = contentService.create(
            ContentModel.create(
                ContentType.sport,
                item.strEvent().strip(),
                item.strFilename().strip(),
                thumbnailPath
            )
        );
        contentTagService.applyTags(content.getId(), tagNames);

        externalMappingRepository.save(
            ContentExternalProvider.TSDB,
            externalId,
            content.getId()
        );

        return true;
    }
}
