package com.mopl.jpa.repository.watchingsession.query;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 시청 세션 인메모리 저장소.
 *
 * - Fake(인메모리) 환경에서 "저장/삭제"와 "목록 조회(커서)"가 같은 데이터를 바라보도록 하는 공용 저장소.
 * - 추후 Redis로 전환 시 이 저장소는 제거되고 Redis 구현체로 대체된다.
 */
public interface WatchingSessionInMemoryStore {

    /**
     * 현재 저장된 "활성 시청 세션" 전체 조회.
     */
    List<WatchingSessionModel> findAll();

    /**
     * 특정 사용자(시청자)의 활성 시청 세션 조회.
     */
    Optional<WatchingSessionModel> findByWatcherId(UUID watcherId);

    /**
     * 특정 시청 세션 저장(업서트).
     * - 동일 watcherId의 세션이 이미 존재하면 교체한다.
     */
    WatchingSessionModel save(WatchingSessionModel session);

    /**
     * 특정 사용자(시청자)의 활성 시청 세션 삭제(LEAVE 처리).
     */
    void deleteByWatcherId(UUID watcherId);
}
