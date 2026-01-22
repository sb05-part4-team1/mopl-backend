package com.mopl.domain.exception.conversation;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConversationErrorCode implements ErrorCode {

    CONVERSATION_ACCESS_DENIED(403, "대화의 참여자가 아닙니다."),
    CONVERSATION_NOT_FOUND(404, "대화가 존재하지 않습니다."),
    DIRECT_MESSAGE_NOT_FOUND(404, "메시지가 존재하지 않습니다."),
    READ_STATUS_NOT_FOUND(404, "읽음 상태가 존재하지 않습니다.");

    private final int status;
    private final String message;

}
