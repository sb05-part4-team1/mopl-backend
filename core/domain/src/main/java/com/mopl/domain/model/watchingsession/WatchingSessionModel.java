package com.mopl.domain.model.watchingsession;

import com.mopl.domain.exception.watchingsession.InvalidWatchingSessionDataException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Builder(toBuilder = true)
public class WatchingSessionModel {

    private UUID watcherId;
    private String watcherName;
    private String watcherProfileImagePath;
    private UUID contentId;
    private String contentTitle;
    private Instant createdAt;
    private int connectionCount;

    public WatchingSessionModel(
        UUID watcherId,
        String watcherName,
        String watcherProfileImagePath,
        UUID contentId,
        String contentTitle,
        Instant createdAt,
        int connectionCount
    ) {
        this.watcherId = watcherId;
        this.watcherName = watcherName;
        this.watcherProfileImagePath = watcherProfileImagePath;
        this.contentId = contentId;
        this.contentTitle = contentTitle;
        this.createdAt = createdAt;
        this.connectionCount = connectionCount;
    }

    public static WatchingSessionModel create(
        UUID watcherId,
        String watcherName,
        String watcherProfileImagePath,
        UUID contentId,
        String contentTitle
    ) {
        if (watcherId == null) {
            throw InvalidWatchingSessionDataException.withDetailMessage("시청자 ID는 null일 수 없습니다.");
        }
        if (contentId == null) {
            throw InvalidWatchingSessionDataException.withDetailMessage("콘텐츠 ID는 null일 수 없습니다.");
        }

        return WatchingSessionModel.builder()
            .watcherId(watcherId)
            .watcherName(watcherName)
            .watcherProfileImagePath(watcherProfileImagePath)
            .contentId(contentId)
            .contentTitle(contentTitle)
            .createdAt(Instant.now())
            .connectionCount(1)
            .build();
    }

    public WatchingSessionModel incrementConnectionCount() {
        return this.toBuilder()
            .connectionCount(this.connectionCount + 1)
            .build();
    }

    public WatchingSessionModel decrementConnectionCount() {
        return this.toBuilder()
            .connectionCount(Math.max(0, this.connectionCount - 1))
            .build();
    }

    public boolean hasNoConnections() {
        return this.connectionCount <= 0;
    }
}
