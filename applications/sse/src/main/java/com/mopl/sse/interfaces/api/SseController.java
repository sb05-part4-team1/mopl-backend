package com.mopl.sse.interfaces.api;

import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.sse.application.SseFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController implements SseApiSpec {

    private final SseFacade sseFacade;

    @GetMapping(produces = "text/event-stream")
    public SseEmitter subscribe(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @RequestParam(required = false) UUID lastEventId
    ) {
        return sseFacade.subscribe(userDetails.userId(), lastEventId);
    }
}
