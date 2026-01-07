package com.mopl.api.interfaces.api.playlist;

import com.mopl.api.interfaces.api.content.ContentSummary;
import com.mopl.api.interfaces.api.content.ContentSummaryMapper;
import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PlaylistResponseMapper {

    private final UserSummaryMapper userSummaryMapper;
    private final ContentSummaryMapper contentSummaryMapper;

    public PlaylistResponse toResponse(
        PlaylistModel model,
        UserModel owner
    ) {
        return toResponse(
            model,
            owner,
            0L,
            false,
            Collections.emptyList()
        );
    }

    public PlaylistResponse toResponse(
        PlaylistModel model,
        UserModel owner,
        long subscriberCount,
        boolean subscribedByMe,
        List<ContentSummary> contents
    ) {
        List<ContentSummary> safeContents = (contents == null) ? Collections.emptyList() : contents;

        return new PlaylistResponse(
            model.getId(),
            userSummaryMapper.toSummary(owner),
            model.getTitle(),
            model.getDescription(),
            model.getUpdatedAt(),
            // TODO: 아래 수치/상태 데이터들은 추후 도메인 로직 구현 시 실제 값으로 대체 필요
            subscriberCount,
            subscribedByMe,
            safeContents
        );
    }
}
