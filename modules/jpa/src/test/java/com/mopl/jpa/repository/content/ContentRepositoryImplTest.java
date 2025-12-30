package com.mopl.jpa.repository.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    JpaConfig.class,
    ContentRepositoryImpl.class,
    ContentEntityMapper.class
})
@DisplayName("ContentRepositoryImpl 슬라이스 테스트")
class ContentRepositoryImplTest {

    @Autowired
    private ContentRepository contentRepository;

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("콘텐츠만 저장한다")
        void saveContent_only() {
            // given
            ContentModel contentModel = ContentModel.create(
                "영화",
                "인셉션",
                "꿈속의 꿈",
                "https://mopl.com/inception.png"
            );

            // when
            ContentModel savedContent = contentRepository.save(contentModel);

            // then
            assertThat(savedContent.getId()).isNotNull();
            assertThat(savedContent.getType()).isEqualTo("영화");
            assertThat(savedContent.getTitle()).isEqualTo("인셉션");
            assertThat(savedContent.getDescription()).isEqualTo("꿈속의 꿈");
            assertThat(savedContent.getThumbnailUrl()).isEqualTo("https://mopl.com/inception.png");
            assertThat(savedContent.getTags()).isEmpty();
            assertThat(savedContent.getCreatedAt()).isNotNull();
            assertThat(savedContent.getUpdatedAt()).isNotNull();
            assertThat(savedContent.getDeletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("existsById()")
    class ExistsByIdTest {

        @Test
        @DisplayName("존재하는 콘텐츠 ID면 true를 반환한다")
        void exists_returnsTrue() {
            // given
            ContentModel saved = contentRepository.save(
                ContentModel.create("영화", "인셉션", "꿈속의 꿈", "url")
            );

            // when
            boolean exists = contentRepository.existsById(saved.getId());

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID면 false를 반환한다")
        void notExists_returnsFalse() {
            // when
            boolean exists = contentRepository.existsById(UUID.randomUUID());

            // then
            assertThat(exists).isFalse();
        }
    }
}
