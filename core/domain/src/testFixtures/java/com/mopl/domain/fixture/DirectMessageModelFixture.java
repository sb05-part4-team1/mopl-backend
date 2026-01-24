package com.mopl.domain.fixture;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

public final class DirectMessageModelFixture {

    private DirectMessageModelFixture() {
    }

    public static ArbitraryBuilder<DirectMessageModel> builder() {
        return fixtureMonkey().giveMeBuilder(DirectMessageModel.class)
            .setNull("conversation")
            .set("sender", UserModelFixture.create())
            .set("receiver", UserModelFixture.create());
    }

    public static DirectMessageModel create() {
        return builder().sample();
    }
}
