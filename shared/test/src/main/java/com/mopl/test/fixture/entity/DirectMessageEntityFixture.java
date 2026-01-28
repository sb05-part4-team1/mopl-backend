package com.mopl.test.fixture.entity;

import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.test.fixture.FixtureMonkeyFactory;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

/**
 * Test fixture for DirectMessageEntity.
 */
public final class DirectMessageEntityFixture {

    private DirectMessageEntityFixture() {
    }

    public static ArbitraryBuilder<DirectMessageEntity> builder() {
        return FixtureMonkeyFactory.jpaEntityMonkey().giveMeBuilder(DirectMessageEntity.class)
            .setNull("id")
            .setNull("createdAt")
            .setNull("deletedAt")
            .set("content", Arbitraries.strings().alpha().ofLength(50));
    }

    public static DirectMessageEntity create(ConversationEntity conversation, UserEntity sender) {
        return builder()
            .set("conversation", conversation)
            .set("sender", sender)
            .sample();
    }

    public static DirectMessageEntity createWithContent(ConversationEntity conversation, UserEntity sender, String content) {
        return builder()
            .set("conversation", conversation)
            .set("sender", sender)
            .set("content", content)
            .sample();
    }
}
