package com.mopl.domain.fixture;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

import com.mopl.domain.model.tag.TagModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

public final class TagModelFixture {

    private TagModelFixture() {
    }

    public static ArbitraryBuilder<TagModel> builder() {
        return fixtureMonkey().giveMeBuilder(TagModel.class)
            .setNull("updatedAt")
            .set("name", Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(10));
    }

    public static TagModel create() {
        return builder().sample();
    }
}
