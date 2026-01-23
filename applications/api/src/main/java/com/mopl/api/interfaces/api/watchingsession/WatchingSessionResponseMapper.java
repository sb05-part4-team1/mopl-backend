package com.mopl.api.interfaces.api.watchingsession;

import org.springframework.stereotype.Component;

import com.mopl.api.interfaces.api.content.mapper.ContentSummaryMapper;
import com.mopl.api.interfaces.api.user.mapper.UserSummaryMapper;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WatchingSessionResponseMapper {

    private final UserSummaryMapper userSummaryMapper;
    private final ContentSummaryMapper contentSummaryMapper;

    public WatchingSessionDto toDto(
        WatchingSessionModel session,
        UserModel watcher,
        ContentModel content,
        List<String> tags
    ) {
        return new WatchingSessionDto(
            session.getId(),
            session.getCreatedAt(),
            userSummaryMapper.toSummary(watcher),
            contentSummaryMapper.toSummary(content, tags)
        );
    }
}
