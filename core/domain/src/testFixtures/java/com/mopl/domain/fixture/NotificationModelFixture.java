package com.mopl.domain.fixture;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.notification.NotificationModel.NotificationLevel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

public final class NotificationModelFixture {

    private NotificationModelFixture() {
    }

    public static ArbitraryBuilder<NotificationModel> builder() {
        return fixtureMonkey().giveMeBuilder(NotificationModel.class)
            .setNull("deletedAt")
            .set("level", NotificationLevel.INFO)
            .set("receiver", UserModelFixture.create());
    }

    public static NotificationModel create() {
        return builder().sample();
    }

    public static NotificationModel createWithLevel(NotificationLevel level) {
        return builder()
            .set("level", level)
            .sample();
    }
}
