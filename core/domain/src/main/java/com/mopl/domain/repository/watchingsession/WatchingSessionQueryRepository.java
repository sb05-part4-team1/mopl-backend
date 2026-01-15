package com.mopl.domain.repository.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.support.cursor.CursorResponse;

import java.util.UUID;

public interface WatchingSessionQueryRepository {

    /**
     * 특정 콘텐츠의 활성 시청 세션 목록 조회 (커서 페이지네이션).
     */
    CursorResponse<WatchingSessionModel> findByContentId(
        UUID contentId,
        WatchingSessionQueryRequest request
    );
}
