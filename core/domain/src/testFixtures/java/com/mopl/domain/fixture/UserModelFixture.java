package com.mopl.domain.fixture;

import com.mopl.domain.model.user.UserModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

import java.util.Locale;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

public final class UserModelFixture {

    private UserModelFixture() {
    }

    public static ArbitraryBuilder<UserModel> builder() {
        return fixtureMonkey().giveMeBuilder(UserModel.class)
            .setNull("deletedAt")
            .set("authProvider", UserModel.AuthProvider.EMAIL)
            .set("email", Arbitraries.strings().alpha().ofLength(8)
                .map(s -> s.toLowerCase(Locale.ROOT) + "@testfixture.com"))
            .setNull("profileImagePath")
            .set("role", UserModel.Role.USER)
            .set("locked", false);
    }

    public static UserModel create() {
        return builder().sample();
    }
}
