package com.mopl.domain.repository.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.support.cursor.CursorResponse;

import java.util.UUID;

public interface WatchingSessionQueryRepository {

    CursorResponse<WatchingSessionModel> findAllByContentId(
        UUID contentId,
        WatchingSessionQueryRequest request
    );
}
