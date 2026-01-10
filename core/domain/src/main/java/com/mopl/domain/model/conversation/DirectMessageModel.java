package com.mopl.domain.model.conversation;

import com.mopl.domain.model.base.BaseModel;
import com.mopl.domain.model.user.UserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessageModel extends BaseModel {

    private ConversationModel conversation;
    private UserModel sender;
    private String content;

//    public DirectMessageModel create(){
//        return DirectMessageModel.builder().build();
//    }

}
