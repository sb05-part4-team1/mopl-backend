package com.mopl.domain.service.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

/**
 * 시청 세션 도메인 서비스.
 * - 활성 시청 세션은 Fake(인메모리) Repository에 저장되어 있다고 가정한다.
 * - 목록 조회는 커서 페이지네이션을 지원한다.
 * - 추후 Redis로 Repository 구현체를 교체해도 Service 시그니처는 유지한다.
 */

@RequiredArgsConstructor
public class WatchingSessionService {

    private final WatchingSessionRepository watchingSessionRepository;
    private final WatchingSessionQueryRepository watchingSessionQueryRepository;

    /**
     * 특정 사용자의 현재 시청 세션 단건 조회(없으면 empty). 200/204
     */
    public Optional<WatchingSessionModel> getWatchingSessionByWatcherId(UUID watcherId) {
        return watchingSessionRepository.findByWatcherId(watcherId);
    }

    /**
     * 특정 콘텐츠의 현재 시청 세션 목록 조회(커서 페이지네이션).
     */
    public CursorResponse<WatchingSessionModel> getWatchingSessions(
        UUID contentId,
        WatchingSessionQueryRequest request
    ) {
        return watchingSessionQueryRepository.findByContentId(contentId, request);
    }

    public long countByContentId(UUID contentId) {
        return watchingSessionRepository.countByContentId(contentId);
    }
}
