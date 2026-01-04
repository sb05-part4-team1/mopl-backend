package com.mopl.domain.fixture;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

import com.mopl.domain.model.content.ContentModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContentFixture {

    public static ArbitraryBuilder<ContentModel> builder() {
        return fixtureMonkey().giveMeBuilder(ContentModel.class);
    }

    public static ContentModel create() {
        return builder().sample();
    }

    public static ContentModel createWithTags(List<String> tags) {
        return builder()
            .set("tags", tags)
            .sample();
    }
}
