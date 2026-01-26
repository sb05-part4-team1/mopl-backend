package com.mopl.domain.fixture;

import com.mopl.domain.model.conversation.ConversationModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

public final class ConversationModelFixture {

    private ConversationModelFixture() {
    }

    public static ArbitraryBuilder<ConversationModel> builder() {
        return fixtureMonkey().giveMeBuilder(ConversationModel.class)
            .setNull("deletedAt");
    }

    public static ConversationModel create() {
        return builder().sample();
    }
}
