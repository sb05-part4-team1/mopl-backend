package com.mopl.dto.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.dto.content.ContentSummary;
import com.mopl.dto.user.UserSummary;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WatchingSessionResponseMapper {

    private final StorageProvider storageProvider;

    public WatchingSessionResponse toResponse(WatchingSessionModel watchingSessionModel) {
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
                0.0,
                0
            )
        );
    }
}
