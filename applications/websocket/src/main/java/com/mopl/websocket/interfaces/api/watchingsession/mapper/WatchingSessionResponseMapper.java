package com.mopl.websocket.interfaces.api.watchingsession.mapper;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.storage.provider.StorageProvider;
import com.mopl.websocket.interfaces.api.content.mapper.ContentSummaryMapper;
import com.mopl.websocket.interfaces.api.user.dto.UserSummary;
import com.mopl.websocket.interfaces.api.watchingsession.dto.WatchingSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WatchingSessionResponseMapper {

    private final StorageProvider storageProvider;
    private final ContentSummaryMapper contentSummaryMapper;

    public WatchingSessionResponse toDto(
        WatchingSessionModel model,
        ContentModel contentModel,
        List<String> tags
    ) {
        return new WatchingSessionResponse(
            model.getWatcherId(),
            model.getCreatedAt(),
            new UserSummary(
                model.getWatcherId(),
                model.getWatcherName(),
                storageProvider.getUrl(model.getWatcherProfileImagePath())
            ),
            contentSummaryMapper.toSummary(contentModel, tags)
        );
    }
}
