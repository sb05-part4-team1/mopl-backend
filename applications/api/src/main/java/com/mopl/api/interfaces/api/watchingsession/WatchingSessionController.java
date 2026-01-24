package com.mopl.api.interfaces.api.watchingsession;

import com.mopl.api.application.watchingsession.WatchingSessionFacade;
import com.mopl.api.interfaces.api.watchingsession.dto.WatchingSessionResponse;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WatchingSessionController implements WatchingSessionApiSpec {

    private final WatchingSessionFacade watchingSessionFacade;

    @Override
    @GetMapping("/contents/{contentId}/watching-sessions")
    public CursorResponse<WatchingSessionResponse> getWatchingSessions(
        @PathVariable UUID contentId,
        WatchingSessionQueryRequest request
    ) {
        return watchingSessionFacade.getWatchingSessions(contentId, request);
    }

    @Override
    @GetMapping("/users/{watcherId}/watching-sessions")
    public WatchingSessionResponse getWatchingSession(@PathVariable UUID watcherId) {
        return watchingSessionFacade.getWatchingSession(watcherId).orElse(null);
    }
}
