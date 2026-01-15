package com.mopl.api.application.watchingsession;

import com.mopl.api.interfaces.api.watchingsession.WatchingSessionDto;
import com.mopl.api.interfaces.api.watchingsession.WatchingSessionResponseMapper;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.service.watchingsession.WatchingSessionService;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WatchingSessionFacade {

    private final UserService userService;
    private final ContentService contentService;
    private final WatchingSessionResponseMapper watchingSessionResponseMapper;
    private final WatchingSessionService watchingSessionService;

    @Transactional
    public Optional<WatchingSessionDto> getWatchingSession(
        UUID requesterId,
        UUID watcherId
    ) {

        // 인증이 잘되었는가
        userService.getById(requesterId);

        // 0) 유저(내가 프로필 들어간 사람) 존재 보장 (없으면 여기서 404 쪽 예외가 터지는 구조)
        UserModel watcher = userService.getById(watcherId);

        // 1) 시청 세션 조회 (지금은 Stub이므로 Optional.empty()일 수 있음)
        Optional<WatchingSessionModel> sessionModelOptional = watchingSessionService
            .getWatchingSessionByWatcherId(watcherId);

        // 2) 시청 중이 아니면 empty 반환 -> Controller가 204 처리
        if (sessionModelOptional.isEmpty()) {
            return Optional.empty();
        }
        WatchingSessionModel session = sessionModelOptional.get();

        // 3) 시청 중이면 content 최신 정보 조회 (없으면 ContentService에서 예외)
        ContentModel contentModel = contentService.getById(session.getContent().getId());

        // 4) 응답 DTO 조립
        WatchingSessionDto response = watchingSessionResponseMapper.toDto(
            session,
            watcher,
            contentModel
        );

        return Optional.of(response);
    }

    // 형식 맞추려했는데 쩔수없이...
    @Transactional(readOnly = true)
    public CursorResponse<WatchingSessionDto> getWatchingSessions(
        UUID contentId,
        WatchingSessionQueryRequest request
    ) {
        return watchingSessionService.getWatchingSessions(contentId, request)
            .map(session -> {
                UserModel watcher = userService.getById(session.getWatcher().getId());
                ContentModel content = contentService.getById(session.getContent().getId());

                return watchingSessionResponseMapper.toDto(
                    session,
                    watcher,
                    content
                );
            });
    }

}
