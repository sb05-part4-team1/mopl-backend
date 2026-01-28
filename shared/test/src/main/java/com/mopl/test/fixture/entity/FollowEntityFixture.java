package com.mopl.test.fixture.entity;

import com.mopl.jpa.entity.follow.FollowEntity;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.test.fixture.FixtureMonkeyFactory;
import com.navercorp.fixturemonkey.ArbitraryBuilder;

/**
 * Test fixture for FollowEntity.
 */
public final class FollowEntityFixture {

    private FollowEntityFixture() {
    }

    public static ArbitraryBuilder<FollowEntity> builder() {
        return FixtureMonkeyFactory.jpaEntityMonkey().giveMeBuilder(FollowEntity.class)
            .setNull("id")
            .setNull("createdAt")
            .setNull("deletedAt");
    }

    public static FollowEntity create(UserEntity follower, UserEntity followee) {
        return builder()
            .set("follower", follower)
            .set("followee", followee)
            .sample();
    }
}
