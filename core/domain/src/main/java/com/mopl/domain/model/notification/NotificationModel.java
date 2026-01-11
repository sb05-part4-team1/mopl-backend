package com.mopl.domain.model.notification;

import java.util.UUID;

import com.mopl.domain.model.base.BaseModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationModel extends BaseModel {

    private String title;
    private String content;
    private NotificationLevel level;
    private UUID receiverId;

    public static NotificationModel create(
        String title,
        String content,
        NotificationLevel level,
        UUID receiverId
    ) {
        return NotificationModel.builder()
            .title(title)
            .content(content)
            .level(level)
            .receiverId(receiverId)
            .build();
    }
}
