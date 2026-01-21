package com.mopl.domain.fixture;

import com.mopl.domain.model.outbox.OutboxModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;

import java.util.UUID;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

public final class OutboxModelFixture {

    private OutboxModelFixture() {
    }

    public static ArbitraryBuilder<OutboxModel> builder() {
        return fixtureMonkey().giveMeBuilder(OutboxModel.class)
            .set("aggregateType", "User")
            .set("aggregateId", UUID.randomUUID().toString())
            .set("eventType", "UserCreated")
            .set("topic", "user-events")
            .set("payload", "{\"userId\":\"" + UUID.randomUUID() + "\"}")
            .set("status", OutboxModel.OutboxStatus.PENDING)
            .set("retryCount", 0)
            .setNull("publishedAt")
            .setNull("deletedAt");
    }

    public static OutboxModel create() {
        return builder().sample();
    }

    public static OutboxModel createWithStatus(OutboxModel.OutboxStatus status) {
        return builder()
            .set("status", status)
            .sample();
    }
}
