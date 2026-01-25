package com.mopl.domain.fixture;

import com.mopl.domain.model.conversation.ReadStatusModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

public final class ReadStatusModelFixture {

    private ReadStatusModelFixture() {
    }

    public static ArbitraryBuilder<ReadStatusModel> builder() {
        return fixtureMonkey().giveMeBuilder(ReadStatusModel.class)
            .set("participant", UserModelFixture.create())
            .set("conversation", ConversationModelFixture.create());
    }

    public static ReadStatusModel create() {
        return builder().sample();
    }
}
