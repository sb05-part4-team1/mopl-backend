package com.mopl.domain.fixture;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

import com.mopl.domain.model.user.FollowModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FollowFixture {

    public static ArbitraryBuilder<FollowModel> builder() {
        return fixtureMonkey().giveMeBuilder(FollowModel.class)
            .setNull("updatedAt");
    }

    public static FollowModel create() {
        return builder().sample();
    }
}
