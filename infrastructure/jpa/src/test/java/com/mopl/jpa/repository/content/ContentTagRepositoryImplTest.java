package com.mopl.jpa.repository.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.entity.content.ContentTagEntity;
import com.mopl.jpa.entity.tag.TagEntityMapper;
import com.mopl.jpa.repository.tag.TagRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Import({
    JpaConfig.class,
    ContentTagRepositoryImpl.class,
    ContentRepositoryImpl.class,
    TagRepositoryImpl.class,
    ContentEntityMapper.class,
    TagEntityMapper.class
})
@DisplayName("ContentTagRepositoryImpl 슬라이스 테스트")
class ContentTagRepositoryImplTest {

    @Autowired
    private ContentTagRepository contentTagRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private JpaContentTagRepository jpaContentTagRepository;

    @Nested
    @DisplayName("saveAll()")
    class SaveAllTest {

        @Test
        @DisplayName("콘텐츠와 태그를 연결한다")
        void saveAll_linksContentAndTags() {
            // given
            ContentModel content = ContentModel.create(
                "영화",
                "인셉션",
                "꿈속의 꿈",
                "https://mopl.com/inception.png"
            );
            ContentModel savedContent = contentRepository.save(content);

            TagModel tag1 = tagRepository.save(TagModel.create("SF"));
            TagModel tag2 = tagRepository.save(TagModel.create("액션"));

            // when
            contentTagRepository.saveAll(
                savedContent.getId(),
                List.of(tag1, tag2)
            );

            // then
            List<ContentTagEntity> contentTags = jpaContentTagRepository.findAll();

            assertThat(contentTags).hasSize(2);

            assertThat(contentTags)
                .allMatch(ct -> ct.getContent().getId()
                    .equals(savedContent.getId()));

            assertThat(contentTags)
                .extracting(ct -> ct.getTag().getName())
                .containsExactlyInAnyOrder("SF", "액션");
        }
    }
}
