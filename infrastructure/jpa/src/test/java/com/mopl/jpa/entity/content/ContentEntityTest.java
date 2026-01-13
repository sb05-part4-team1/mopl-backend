package com.mopl.jpa.entity.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.repository.content.ContentRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    JpaConfig.class,
    ContentRepositoryImpl.class,
    ContentEntityMapper.class
})
@DisplayName("ContentEntity 슬라이스 테스트")
class ContentEntityTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private ContentRepository contentRepository;

    @Nested
    @DisplayName("엔티티 저장")
    class PersistTest {

        @Test
        @DisplayName("저장 시 id(UUID v7)가 자동 생성된다")
        void persist_generatesUuidV7() {
            // given
            ContentEntity entity = createContentEntity();

            // when
            testEntityManager.persistAndFlush(entity);

            // then
            assertThat(entity.getId()).isNotNull();
            assertThat(entity.getId().version()).isEqualTo(7);
        }

        @Test
        @DisplayName("저장 시 createdAt이 자동 설정된다")
        void persist_setsCreatedAt() {
            // given
            ContentEntity entity = createContentEntity();

            // when
            testEntityManager.persistAndFlush(entity);

            // then
            assertThat(entity.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("저장 시 updatedAt이 자동 설정된다")
        void persist_setsUpdatedAt() {
            // given
            ContentEntity entity = createContentEntity();

            // when
            testEntityManager.persistAndFlush(entity);

            // then
            assertThat(entity.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("엔티티 수정")
    class UpdateTest {

        @Test
        @DisplayName("dirty checking으로 수정 시 updatedAt이 갱신된다")
        void update_updatesUpdatedAt() {
            // given
            ContentEntity entity = createContentEntity();
            testEntityManager.persistAndFlush(entity);
            Instant originalUpdatedAt = entity.getUpdatedAt();

            // when
            ReflectionTestUtils.setField(entity, "title", "수정된 제목");
            testEntityManager.flush();
            testEntityManager.clear();

            // then
            ContentEntity updated = testEntityManager.find(ContentEntity.class, entity.getId());

            assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
            assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        }
    }

    @Nested
    @DisplayName("@SQLRestriction (Soft Delete)")
    class SoftDeleteTest {

        @Test
        @DisplayName("deletedAt이 null이 아니면 조회되지 않는다")
        void deletedEntity_isExcludedFromQuery() {
            // given
            ContentEntity entity = ContentEntity.builder()
                .type(ContentModel.ContentType.movie)
                .title("삭제된 콘텐츠")
                .description("설명")
                .thumbnailUrl("url")
                .deletedAt(Instant.now())
                .build();

            testEntityManager.persistAndFlush(entity);
            testEntityManager.clear();

            // when
            Optional<?> result = contentRepository.findById(entity.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deletedAt이 null이면 정상 조회된다")
        void notDeletedEntity_isIncludedInQuery() {
            // given
            ContentEntity entity = createContentEntity();
            testEntityManager.persistAndFlush(entity);
            testEntityManager.clear();

            // when
            Optional<?> result = contentRepository.findById(entity.getId());

            // then
            assertThat(result).isPresent();
        }
    }

    private ContentEntity createContentEntity() {
        return ContentEntity.builder()
            .type(ContentModel.ContentType.movie)
            .title("인셉션")
            .description("꿈속의 꿈")
            .thumbnailUrl("https://mopl.com/inception.png")
            .build();
    }
}
