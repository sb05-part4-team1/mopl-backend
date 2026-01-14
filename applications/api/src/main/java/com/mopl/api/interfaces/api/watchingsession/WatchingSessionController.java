package com.mopl.api.interfaces.api.watchingsession;

import com.mopl.api.application.watchingsession.WatchingSessionFacade;
import com.mopl.security.userdetails.MoplUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class WatchingSessionController {

    private final WatchingSessionFacade watchingSessionFacade;

    @GetMapping("/{watcherId}/watching-sessions")
    public ResponseEntity<WatchingSessionDto> getWatchingSession( // 여기만 예외로 ResponseEntity를 사용 (응답이 2개여서)
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID watcherId
    ) {

        UUID requesterId = userDetails.userId();

        return watchingSessionFacade.getWatchingSession(requesterId, watcherId)
            .map(ResponseEntity::ok)    // 시청 중이면 200 + body
            .orElseGet(() -> ResponseEntity.noContent().build()); // 시청 안 하면 204
    }
}
