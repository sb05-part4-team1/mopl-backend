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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
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
    @DisplayName("findById()")
    class FindByIdTest {

        @Test
        @DisplayName("저장된 콘텐츠를 ID로 조회하면 해당 모델을 반환한다")
        void findById_returnsContentModel() {
            // given
            ContentModel original = ContentModel.create(
                ContentModel.ContentType.movie,
                "인셉션",
                "꿈속의 꿈",
                "https://mopl.com/inception.png"
            );
            ContentModel saved = contentRepository.save(original);

            // when
            Optional<ContentModel> found = contentRepository.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
            assertThat(found.get().getTitle()).isEqualTo("인셉션");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void findById_withNonExistentId_returnsEmpty() {
            // when
            Optional<ContentModel> found = contentRepository.findById(UUID.randomUUID());

            // then
            assertThat(found).isEmpty();
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
                ContentModel.create(ContentModel.ContentType.movie, "인셉션", "꿈속의 꿈", "url")
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

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("콘텐츠만 저장한다")
        void saveContent_only() {
            // given
            ContentModel contentModel = ContentModel.create(
                ContentModel.ContentType.movie,
                "인셉션",
                "꿈속의 꿈",
                "https://mopl.com/inception.png"
            );

            // when
            ContentModel savedContent = contentRepository.save(contentModel);

            // then
            assertThat(savedContent.getId()).isNotNull();
            assertThat(savedContent.getType()).isEqualTo(ContentModel.ContentType.movie);
            assertThat(savedContent.getTitle()).isEqualTo("인셉션");
            assertThat(savedContent.getDescription()).isEqualTo("꿈속의 꿈");
            assertThat(savedContent.getThumbnailUrl()).isEqualTo("https://mopl.com/inception.png");
            assertThat(savedContent.getCreatedAt()).isNotNull();
            assertThat(savedContent.getUpdatedAt()).isNotNull();
            assertThat(savedContent.getDeletedAt()).isNull();
        }
    }
}
