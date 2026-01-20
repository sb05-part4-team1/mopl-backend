package com.mopl.domain.exception.conversation;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConversationErrorCode implements ErrorCode {

    CONVERSATION_NOT_FOUND(404, "대화가 존재하지 않습니다."),
    DIRECTMESSAGE_NOT_FOUND(404, "메시지가 존재하지 않습니다."),
    READSTATUS_NOT_FOUND(404, "읽음상태가 존재하지 않습니다."),
    CONVERSATION_NOT_PARTICIPANT(403, "대화의 참여자가 아닙니다.");

    private final int status;
    private final String message;

}
