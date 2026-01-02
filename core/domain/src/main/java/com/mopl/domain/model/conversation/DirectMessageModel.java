package com.mopl.domain.model.conversation;

import com.mopl.domain.model.base.BaseModel;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessageModel extends BaseModel {

    private UUID conversationId;
    private UUID senderId;
    private UUID receiverId;
    private String content;

//    public DirectMessageModel create(){
//        return DirectMessageModel.builder().build();
//    }

}
