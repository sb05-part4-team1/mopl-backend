package com.mopl.jpa.repository.watchingsession;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FakeWatchingSessionRepository implements WatchingSessionRepository {

    // 내가 임시로 테스트할 때 DB에 값을 넣어서 사용중인 데이터
    private static final UUID FAKE_CONTENT_ID = UUID.fromString(
        "22222222-2222-2222-2222-222222222222");

    @Override
    public Optional<WatchingSessionModel> findByWatcherId(UUID watcherId) {

        // 여기서 조건 걸어도 되고, 그냥 항상 반환해도 됨
        UserModel fakeWatcher = UserModel.builder()
            .id(watcherId)
            .build();

        ContentModel fakeContent = ContentModel.builder()
            .id(FAKE_CONTENT_ID)
            .build();

        WatchingSessionModel session = WatchingSessionModel.builder()
            .id(UUID.randomUUID()) // 이게맞나? (Entity를 안쓰기 때문)
            .createdAt(Instant.now()) // 이게 맞나? (Entity를 안쓰기 때문)
            .watcher(fakeWatcher)
            .content(fakeContent)
            .build();

        return Optional.of(session);
    }

}
