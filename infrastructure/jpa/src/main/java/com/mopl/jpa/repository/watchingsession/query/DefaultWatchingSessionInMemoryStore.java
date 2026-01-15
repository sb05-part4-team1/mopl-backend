package com.mopl.jpa.repository.watchingsession.query;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * WatchingSessionInMemoryStore 기본 구현체.
 *
 * - Key: watcherId
 * - Value: WatchingSessionModel (활성 세션)
 *
 * 주의:
 * - 목록 조회는 values()를 snapshot으로 복사해서 반환한다.
 * - 저장/삭제는 thread-safe 하게 동작한다.
 */
@Component
@RequiredArgsConstructor
public class DefaultWatchingSessionInMemoryStore implements WatchingSessionInMemoryStore {

    private final ConcurrentMap<UUID, WatchingSessionModel> store = new ConcurrentHashMap<>();

    @Override
    public List<WatchingSessionModel> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<WatchingSessionModel> findByWatcherId(UUID watcherId) {
        if (watcherId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(watcherId));
    }

    @Override
    public WatchingSessionModel save(WatchingSessionModel session) {
        if (session == null || session.getWatcher() == null || session.getWatcher().getId()
            == null) {
            throw new IllegalArgumentException("시청 세션 저장 시 watcherId는 null일 수 없습니다.");
        }

        UUID watcherId = session.getWatcher().getId();
        store.put(watcherId, session);

        return session;
    }

    @Override
    public void deleteByWatcherId(UUID watcherId) {
        if (watcherId == null) {
            return;
        }
        store.remove(watcherId);
    }
}
