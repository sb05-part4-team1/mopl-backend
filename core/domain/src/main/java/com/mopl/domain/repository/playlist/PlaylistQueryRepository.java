package com.mopl.domain.repository.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.support.cursor.CursorResponse;

public interface PlaylistQueryRepository {

    CursorResponse<PlaylistModel> findAll(PlaylistQueryRequest request);
}
