package com.mopl.dto.playlist;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.dto.content.ContentSummaryMapper;
import com.mopl.dto.user.UserSummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlaylistResponseMapper {

    private final UserSummaryMapper userSummaryMapper;
    private final ContentSummaryMapper contentSummaryMapper;

    public PlaylistResponse toResponse(PlaylistModel playlistModel) {
        return toResponse(
            playlistModel,
            false,
            Collections.emptyList(),
            Map.of()
        );
    }

    public PlaylistResponse toResponse(
        PlaylistModel playlistModel,
        boolean subscribedByMe,
        Collection<ContentModel> contentModels,
        Map<UUID, List<String>> tagsByContentId
    ) {
        return new PlaylistResponse(
            playlistModel.getId(),
            userSummaryMapper.toSummary(playlistModel.getOwner()),
            playlistModel.getTitle(),
            playlistModel.getDescription(),
            playlistModel.getUpdatedAt(),
            playlistModel.getSubscriberCount(),
            subscribedByMe,
            contentSummaryMapper.toSummaries(contentModels, tagsByContentId)
        );
    }
}
