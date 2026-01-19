package com.mopl.domain.event.message;

import com.mopl.domain.event.AbstractDomainEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
public class DirectMessageReceivedEvent extends AbstractDomainEvent {

    private final UUID messageId;
    private final UUID conversationId;
    private final UUID senderId;
    private final String senderName;
    private final UUID receiverId;
    private final String messagePreview;

    @Override
    public String getAggregateType() {
        return "MESSAGE";
    }

    @Override
    public String getAggregateId() {
        return messageId.toString();
    }
}
