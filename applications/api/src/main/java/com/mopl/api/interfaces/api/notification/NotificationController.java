package com.mopl.api.interfaces.api.notification;

import com.mopl.api.application.notification.NotificationFacade;
import com.mopl.dto.notification.NotificationResponse;
import com.mopl.domain.repository.notification.NotificationQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApiSpec {

    private final NotificationFacade notificationFacade;

    @GetMapping
    public CursorResponse<NotificationResponse> getNotifications(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @ModelAttribute NotificationQueryRequest request
    ) {
        return notificationFacade.getNotifications(userDetails.userId(), request);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readNotification(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID notificationId
    ) {
        notificationFacade.readNotification(userDetails.userId(), notificationId);
    }
}
