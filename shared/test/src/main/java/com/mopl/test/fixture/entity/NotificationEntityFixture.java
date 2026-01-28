package com.mopl.test.fixture.entity;

import com.mopl.domain.model.notification.NotificationModel.NotificationLevel;
import com.mopl.jpa.entity.notification.NotificationEntity;
import com.mopl.test.fixture.FixtureMonkeyFactory;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

import java.util.UUID;

/**
 * Test fixture for NotificationEntity.
 */
public final class NotificationEntityFixture {

    private NotificationEntityFixture() {
    }

    public static ArbitraryBuilder<NotificationEntity> builder() {
        return FixtureMonkeyFactory.jpaEntityMonkey().giveMeBuilder(NotificationEntity.class)
            .setNull("id")
            .setNull("createdAt")
            .setNull("deletedAt")
            .set("title", Arbitraries.strings().alpha().ofLength(20))
            .set("content", Arbitraries.strings().alpha().ofLength(50))
            .set("level", NotificationLevel.INFO);
    }

    public static NotificationEntity create(UUID receiverId) {
        return builder()
            .set("receiverId", receiverId)
            .sample();
    }

    public static NotificationEntity createWithLevel(UUID receiverId, NotificationLevel level) {
        return builder()
            .set("receiverId", receiverId)
            .set("level", level)
            .sample();
    }
}
