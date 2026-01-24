package com.mopl.api.interfaces.api.watchingsession.mapper;

import com.mopl.api.interfaces.api.content.dto.ContentSummary;
import com.mopl.api.interfaces.api.user.dto.UserSummary;
import com.mopl.api.interfaces.api.watchingsession.dto.WatchingSessionResponse;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WatchingSessionResponseMapper {

    private final StorageProvider storageProvider;

    public WatchingSessionResponse toDto(WatchingSessionModel watchingSessionModel) {
        return new WatchingSessionResponse(
            watchingSessionModel.getWatcherId(),
            watchingSessionModel.getCreatedAt(),
            new UserSummary(
                watchingSessionModel.getWatcherId(),
                watchingSessionModel.getWatcherName(),
                storageProvider.getUrl(watchingSessionModel.getWatcherProfileImagePath())
            ),
            new ContentSummary(
                watchingSessionModel.getContentId(),
                null,
                watchingSessionModel.getContentTitle(),
                null,
                null,
                List.of(),
                0L,
                0
            )
        );
    }
}
