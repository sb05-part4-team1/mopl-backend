package com.mopl.api.interfaces.api.notification;

import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.repository.notification.NotificationQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Notification API", description = "알림 API")
public interface NotificationApiSpec {

    @Operation(summary = "알림 목록 조회")
    @ApiResponse(
        responseCode = "200",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    CursorResponse<NotificationResponse> getNotifications(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        NotificationQueryRequest request
    );

    @Operation(summary = "알림 읽음 처리")
    @ApiResponse(
        responseCode = "200",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "204",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    void readNotification(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID notificationId
    );
}
