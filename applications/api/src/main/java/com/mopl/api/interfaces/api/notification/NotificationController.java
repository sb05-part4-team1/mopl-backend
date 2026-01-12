package com.mopl.api.interfaces.api.notification;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.api.application.notification.NotificationFacade;
import com.mopl.security.userdetails.MoplUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApiSpec {

    private final NotificationFacade notificationFacade;

    @Override
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> readNotification(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID notificationId
    ) {
        notificationFacade.readNotification(userDetails.userId(), notificationId);
        return ResponseEntity.noContent().build();
    }
}
