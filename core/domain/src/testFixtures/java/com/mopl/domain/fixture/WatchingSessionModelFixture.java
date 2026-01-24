package com.mopl.domain.fixture;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

import java.util.Locale;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

public final class WatchingSessionModelFixture {

    private WatchingSessionModelFixture() {
    }

    public static ArbitraryBuilder<WatchingSessionModel> builder() {
        return fixtureMonkey().giveMeBuilder(WatchingSessionModel.class)
            .set("watcherName", Arbitraries.strings().alpha().ofLength(8)
                .map(s -> "User_" + s))
            .set("watcherProfileImagePath", Arbitraries.strings().alpha().ofLength(10)
                .map(s -> "profiles/" + s.toLowerCase(Locale.ROOT) + ".jpg"))
            .set("contentTitle", Arbitraries.strings().alpha().ofLength(10)
                .map(s -> "Content_" + s));
    }

    public static WatchingSessionModel create() {
        return builder().sample();
    }
}
