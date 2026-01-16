package com.mopl.api.application.playlist;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;

import java.util.List;

public record PlaylistDetail(
    PlaylistModel playlist,
    long subscriberCount,
    boolean subscribedByMe,
    List<ContentModel> contents
) {
}
