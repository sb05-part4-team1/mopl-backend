package com.mopl.jpa.repository.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.jpa.repository.watchingsession.query.WatchingSessionInMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FakeWatchingSessionRepository implements WatchingSessionRepository {

    private final WatchingSessionInMemoryStore watchingSessionInMemoryStore;

    @Override
    public Optional<WatchingSessionModel> findByWatcherId(UUID watcherId) {
        // store에 존재하면 Optional.of(...)
        // store에 없으면 Optional.empty() -> Controller에서 204 반환
        return watchingSessionInMemoryStore.findByWatcherId(watcherId);
    }
}
