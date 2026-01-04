package com.mopl.domain.fixture;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

import com.mopl.domain.model.user.UserModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

public final class UserFixture {

    private UserFixture() {
    }

    public static ArbitraryBuilder<UserModel> builder() {
        return fixtureMonkey().giveMeBuilder(UserModel.class)
            .setNull("deletedAt")
            .set("authProvider", UserModel.AuthProvider.EMAIL)
            .set("email", Arbitraries.strings().alpha().ofLength(8)
                .map(s -> s.toLowerCase() + "@testfixture.com"))
            .setNull("profileImageUrl")
            .set("role", UserModel.Role.USER)
            .set("locked", false);
    }

    public static UserModel create() {
        return builder().sample();
    }
}
