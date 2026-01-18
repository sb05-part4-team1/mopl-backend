package com.mopl.domain.fixture;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

public final class PlaylistModelFixture {

    private PlaylistModelFixture() {
    }

    public static ArbitraryBuilder<PlaylistModel> builder() {
        return fixtureMonkey().giveMeBuilder(PlaylistModel.class)
            .setNull("deletedAt")
            .set("owner", UserModelFixture.create())
            .set("title", Arbitraries.strings().alpha().ofLength(10).map(s -> "플레이리스트_" + s))
            .set("description", Arbitraries.strings().alpha().ofLength(20)
                .map(s -> "테스트 설명_" + s));
    }

    public static ArbitraryBuilder<PlaylistModel> builder(UserModel owner) {
        return builder().set("owner", owner);
    }

    public static PlaylistModel create() {
        return builder().sample();
    }

    public static PlaylistModel create(UserModel owner) {
        return builder(owner).sample();
    }
}
