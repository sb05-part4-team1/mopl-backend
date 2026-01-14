package com.mopl.domain.fixture;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

import com.mopl.domain.model.notification.NotificationLevel;
import com.mopl.domain.model.notification.NotificationModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;

public final class NotificationModelFixture {

    private NotificationModelFixture() {
    }

    public static ArbitraryBuilder<NotificationModel> builder() {
        return fixtureMonkey().giveMeBuilder(NotificationModel.class)
            .setNull("deletedAt")
            .set("level", NotificationLevel.INFO);
    }

    public static NotificationModel create() {
        return builder().sample();
    }
}
