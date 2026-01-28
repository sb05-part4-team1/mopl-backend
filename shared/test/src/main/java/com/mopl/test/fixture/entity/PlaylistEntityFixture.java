package com.mopl.test.fixture.entity;

import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.test.fixture.FixtureMonkeyFactory;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

/**
 * Test fixture for PlaylistEntity.
 */
public final class PlaylistEntityFixture {

    private PlaylistEntityFixture() {
    }

    public static ArbitraryBuilder<PlaylistEntity> builder() {
        return FixtureMonkeyFactory.jpaEntityMonkey().giveMeBuilder(PlaylistEntity.class)
            .setNull("id")
            .setNull("createdAt")
            .setNull("updatedAt")
            .setNull("deletedAt")
            .set("title", Arbitraries.strings().alpha().ofLength(10))
            .set("description", Arbitraries.strings().alpha().ofLength(30))
            .set("subscriberCount", 0);
    }

    public static PlaylistEntity create(UserEntity owner) {
        return builder()
            .set("owner", owner)
            .sample();
    }

    public static PlaylistEntity createWithTitle(UserEntity owner, String title) {
        return builder()
            .set("owner", owner)
            .set("title", title)
            .sample();
    }
}
