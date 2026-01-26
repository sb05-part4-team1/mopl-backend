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
        @DisplayName("존재하는 콘텐츠 ID로 조회하면 ContentModel 반환")
        void withExistingContentId_returnsContentModel() {
            // given
            ContentModel savedContent = contentRepository.save(
                ContentModel.create(
                    ContentModel.ContentType.movie,
                    "인셉션",
                    "꿈속의 꿈",
                    "contents/inception.png"
                )
            );

            // when
            Optional<ContentModel> foundContent = contentRepository.findById(savedContent.getId());

            // then
            assertThat(foundContent).isPresent();
            assertThat(foundContent.get().getId()).isEqualTo(savedContent.getId());
            assertThat(foundContent.get().getTitle()).isEqualTo("인셉션");
            assertThat(foundContent.get().getDescription()).isEqualTo("꿈속의 꿈");
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID로 조회하면 빈 Optional 반환")
        void withNonExistingContentId_returnsEmptyOptional() {
            // given
            UUID nonExistingId = UUID.randomUUID();

            // when
            Optional<ContentModel> foundContent = contentRepository.findById(nonExistingId);

            // then
            assertThat(foundContent).isEmpty();
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 콘텐츠 저장")
        void withNewContent_savesAndReturnsContent() {
            // given
            ContentModel contentModel = ContentModel.create(
                ContentModel.ContentType.movie,
                "인셉션",
                "꿈속의 꿈",
                "contents/inception.png"
            );

            // when
            ContentModel savedContent = contentRepository.save(contentModel);

            // then
            assertThat(savedContent.getId()).isNotNull();
            assertThat(savedContent.getType()).isEqualTo(ContentModel.ContentType.movie);
            assertThat(savedContent.getTitle()).isEqualTo("인셉션");
            assertThat(savedContent.getDescription()).isEqualTo("꿈속의 꿈");
            assertThat(savedContent.getThumbnailPath()).isEqualTo("contents/inception.png");
            assertThat(savedContent.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("기존 콘텐츠 업데이트")
        void withExistingContent_updatesAndReturnsContent() {
            // given
            ContentModel contentModel = ContentModel.create(
                ContentModel.ContentType.movie,
                "인셉션",
                "꿈속의 꿈",
                "contents/inception.png"
            );
            ContentModel savedContent = contentRepository.save(contentModel);

            // when
            ContentModel updatedContent = contentRepository.save(
                savedContent.update("인셉션 2", "더 깊은 꿈", null)
            );

            // then
            assertThat(updatedContent.getId()).isEqualTo(savedContent.getId());
            assertThat(updatedContent.getTitle()).isEqualTo("인셉션 2");
            assertThat(updatedContent.getDescription()).isEqualTo("더 깊은 꿈");
            assertThat(updatedContent.getThumbnailPath()).isEqualTo("contents/inception.png");
        }
    }
}
