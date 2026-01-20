package com.mopl.api.interfaces.api.playlist;

import com.mopl.api.interfaces.api.content.ContentSummaryMapper;
import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class PlaylistResponseMapper {

    private final UserSummaryMapper userSummaryMapper;
    private final ContentSummaryMapper contentSummaryMapper;

    public PlaylistResponse toResponse(PlaylistModel model) {
        return toResponse(
            model,
            0L,
            false,
            Collections.emptyList()
        );
    }

    public PlaylistResponse toResponse(
        PlaylistModel model,
        long subscriberCount,
        boolean subscribedByMe,
        Collection<ContentModel> contentModels
    ) {
        return new PlaylistResponse(
            model.getId(),
            userSummaryMapper.toSummary(model.getOwner()),
            model.getTitle(),
            model.getDescription(),
            model.getUpdatedAt(),
            subscriberCount,
            subscribedByMe,
            contentSummaryMapper.toSummaries(contentModels)
        );
    }
}
