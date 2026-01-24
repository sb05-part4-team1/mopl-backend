package com.mopl.domain.model.conversation;

import com.mopl.domain.model.base.BaseUpdatableModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class ConversationModel extends BaseUpdatableModel {

    public static ConversationModel create() {
        return ConversationModel.builder().build();
    }
}
