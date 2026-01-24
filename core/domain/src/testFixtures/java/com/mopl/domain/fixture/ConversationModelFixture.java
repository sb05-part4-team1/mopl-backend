package com.mopl.domain.fixture;

import com.mopl.domain.model.conversation.ConversationModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

public final class ConversationModelFixture {

    private ConversationModelFixture() {
    }

    public static ArbitraryBuilder<ConversationModel> builder() {
        return fixtureMonkey().giveMeBuilder(ConversationModel.class)
            .setNull("deletedAt")
            .set("withUser", UserModelFixture.create())
            .set("lastMessage", DirectMessageModelFixture.create())
            .set("hasUnread", false);
    }

    public static ConversationModel create() {
        return builder().sample();
    }

    public static ConversationModel createWithoutLastMessage() {
        return builder()
            .setNull("lastMessage")
            .sample();
    }
}
