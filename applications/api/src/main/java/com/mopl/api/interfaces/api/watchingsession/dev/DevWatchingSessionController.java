package com.mopl.api.interfaces.api.watchingsession.dev;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.jpa.repository.watchingsession.query.WatchingSessionInMemoryStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * local 환경에서만 사용하는 Dev API.
 *
 * WebSocket(JOIN/LEAVE) 구현 전까지,
 * 인메모리 store에 시청 세션을 직접 넣고/삭제해서 조회 API를 테스트한다.
 */
@Profile("local")
@RestController
@RequestMapping("/api/dev/watchingsessions")
@RequiredArgsConstructor
public class DevWatchingSessionController {

    private final WatchingSessionInMemoryStore watchingSessionInMemoryStore;

    /**
     * 시청 세션 업서트(저장/갱신)
     * - 동일 watcherId가 있으면 덮어쓴다.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void upsert(@RequestBody @Valid DevWatchingSessionUpsertRequest request) {

        UserModel watcher = UserModel.builder()
            .id(request.watcherId())
            .name(request.watcherName())
            .build();

        ContentModel content = ContentModel.builder()
            .id(request.contentId() != null ? request.contentId() : UUID.randomUUID())
            .title(request.contentTitle())
            .build();

        WatchingSessionModel session = WatchingSessionModel.builder()
            .id(UUID.randomUUID())
            .createdAt(Instant.now())
            .watcher(watcher)
            .content(content)
            .build();

        watchingSessionInMemoryStore.save(session);
    }

    /**
     * 시청 세션 삭제(LEAVE 시뮬레이션)
     */
    @DeleteMapping("/{watcherId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByWatcherId(@PathVariable UUID watcherId) {
        watchingSessionInMemoryStore.deleteByWatcherId(watcherId);
    }
}
