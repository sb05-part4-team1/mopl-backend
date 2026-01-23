package com.mopl.sse.interfaces.api;

import com.mopl.security.userdetails.MoplUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Tag(name = "SSE API", description = "SSE API")
public interface SseApiSpec {

    @Operation(summary = "SSE 구독")
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(
            mediaType = "text/event-stream",
            schema = @Schema(implementation = SseEmitter.class)
        )
    )
    SseEmitter subscribe(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        @Parameter UUID lastEventId
    );
}
