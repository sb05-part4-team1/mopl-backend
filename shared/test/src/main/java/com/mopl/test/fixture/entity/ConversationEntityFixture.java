package com.mopl.test.fixture.entity;

import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.test.fixture.FixtureMonkeyFactory;
import com.navercorp.fixturemonkey.ArbitraryBuilder;

/**
 * Test fixture for ConversationEntity.
 */
public final class ConversationEntityFixture {

    private ConversationEntityFixture() {
    }

    public static ArbitraryBuilder<ConversationEntity> builder() {
        return FixtureMonkeyFactory.jpaEntityMonkey().giveMeBuilder(ConversationEntity.class)
            .setNull("id")
            .setNull("createdAt")
            .setNull("updatedAt")
            .setNull("deletedAt");
    }

    public static ConversationEntity create() {
        return builder().sample();
    }
}
