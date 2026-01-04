package com.mopl.domain.fixture;

import com.mopl.domain.model.user.UserModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.jqwik.api.Arbitraries;

import java.time.Instant;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserFixture {

    public static ArbitraryBuilder<UserModel> builder() {
        Instant now = Instant.now();
        return fixtureMonkey().giveMeBuilder(UserModel.class)
            .set("deletedAt", null)
            .set("authProvider", UserModel.AuthProvider.EMAIL)
            .set("email", Arbitraries.strings().alpha().ofLength(8)
                .map(s -> s.toLowerCase() + "@testfixture.com"))
            .set("profileImageUrl", null)
            .set("role", UserModel.Role.USER)
            .set("locked", false);
    }

    public static UserModel create() {
        return builder().sample();
    }

    public static UserModel createWithAuthProvider(UserModel.AuthProvider authProvider) {
        return builder()
            .set("authProvider", authProvider)
            .sample();
    }

    public static UserModel createWithEmail(String email) {
        return builder()
            .set("email", email)
            .sample();
    }

    public static UserModel createWithName(String name) {
        return builder()
            .set("name", name)
            .sample();
    }

    public static UserModel createWithRole(UserModel.Role role) {
        return builder()
            .set("role", role)
            .sample();
    }

    public static UserModel createLocked() {
        return builder()
            .set("locked", true)
            .sample();
    }

    public static UserModel createDeleted() {
        return builder()
            .set("deletedAt", Instant.now())
            .sample();
    }
}
