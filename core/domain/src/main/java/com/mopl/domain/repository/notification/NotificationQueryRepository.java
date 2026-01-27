package com.mopl.domain.repository.notification;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.support.cursor.CursorResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationQueryRepository {

    CursorResponse<NotificationModel> findAll(UUID receiverId, NotificationQueryRequest request);

    List<NotificationModel> findByReceiverIdAndCreatedAtAfter(UUID receiverId, Instant createdAfter);
}
