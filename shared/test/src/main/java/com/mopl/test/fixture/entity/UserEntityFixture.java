package com.mopl.test.fixture.entity;

import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.test.fixture.FixtureMonkeyFactory;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

import java.util.Locale;

/**
 * Test fixture for UserEntity.
 */
public final class UserEntityFixture {

    private UserEntityFixture() {
    }

    public static ArbitraryBuilder<UserEntity> builder() {
        return FixtureMonkeyFactory.jpaEntityMonkey().giveMeBuilder(UserEntity.class)
            .setNull("id")
            .setNull("createdAt")
            .setNull("updatedAt")
            .setNull("deletedAt")
            .set("authProvider", UserModel.AuthProvider.EMAIL)
            .set("email", Arbitraries.strings().alpha().ofLength(8)
                .map(s -> s.toLowerCase(Locale.ROOT) + "@test.com"))
            .set("name", Arbitraries.strings().alpha().ofLength(6))
            .setNull("password")
            .setNull("profileImagePath")
            .set("role", UserModel.Role.USER)
            .set("locked", false);
    }

    public static UserEntity create() {
        return builder().sample();
    }

    public static UserEntity createAdmin() {
        return builder()
            .set("role", UserModel.Role.ADMIN)
            .sample();
    }

    public static UserEntity createWithEmail(String email) {
        return builder()
            .set("email", email)
            .sample();
    }
}
