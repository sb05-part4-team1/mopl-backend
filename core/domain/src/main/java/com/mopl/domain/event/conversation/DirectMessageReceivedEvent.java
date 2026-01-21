package com.mopl.domain.event.conversation;

import com.mopl.domain.event.DomainEvent;
import com.mopl.domain.event.EventTopic;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DirectMessageReceivedEvent implements DomainEvent {

    private final UUID messageId;
    private final UUID conversationId;
    private final UUID senderId;
    private final String senderName;
    private final UUID receiverId;
    private final String messageContent;

    @Override
    public String getAggregateType() {
        return "MESSAGE";
    }

    @Override
    public String getAggregateId() {
        return messageId.toString();
    }

    @Override
    public String getTopic() {
        return EventTopic.DIRECT_MESSAGE_RECEIVED;
    }
}
