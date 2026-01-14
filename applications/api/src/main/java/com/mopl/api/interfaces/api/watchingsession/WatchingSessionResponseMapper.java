package com.mopl.api.interfaces.api.watchingsession;

import com.mopl.api.interfaces.api.content.ContentSummaryMapper;
import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WatchingSessionResponseMapper {

    private final UserSummaryMapper userSummaryMapper;
    private final ContentSummaryMapper contentSummaryMapper;

    public WatchingSessionDto toDto(
        WatchingSessionModel session,
        UserModel watcher,
        ContentModel content
    ) {
        return toDto(
            session,
            watcher,
            content,
            // TODO: 바꿔야 하지않나 나중에 아마 수요일까진
            0.0,
            0
        );
    }

    public WatchingSessionDto toDto(
        WatchingSessionModel session,
        UserModel watcher,
        ContentModel content,
        double averageRating,
        int reviewCount
    ) {
        return new WatchingSessionDto(
            session.getId(),
            session.getCreatedAt(),
            userSummaryMapper.toSummary(watcher),
            contentSummaryMapper.toSummary(content, averageRating, reviewCount)
        );
    }
}
