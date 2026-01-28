package com.mopl.jpa.repository.content.batch;

import com.mopl.domain.model.content.ContentExternalProvider;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.batch.ContentExternalMappingRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.repository.content.JpaContentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    ContentExternalMappingRepositoryImpl.class
})
@DisplayName("ContentExternalMappingRepositoryImpl 슬라이스 테스트")
class ContentExternalMappingRepositoryImplTest {

    @Autowired
    private ContentExternalMappingRepository contentExternalMappingRepository;

    @Autowired
    private JpaContentRepository jpaContentRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID contentId;

    @BeforeEach
    void setUp() {
        Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
        ContentEntity content = ContentEntity.builder()
            .createdAt(baseTime)
            .updatedAt(baseTime)
            .type(ContentModel.ContentType.movie)
            .title("Test Movie")
            .description("Test Description")
            .thumbnailPath("/test/path.jpg")
            .reviewCount(0)
            .averageRating(0.0)
            .popularityScore(0.0)
            .build();
        ContentEntity savedContent = jpaContentRepository.save(content);
        contentId = savedContent.getId();
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("외부 매핑을 저장한다")
        void savesExternalMapping() {
            // when
            contentExternalMappingRepository.save(
                ContentExternalProvider.TMDB,
                12345L,
                contentId
            );

            // then
            boolean exists = contentExternalMappingRepository.exists(
                ContentExternalProvider.TMDB,
                12345L
            );
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("여러 provider의 매핑을 저장할 수 있다")
        void savesMultipleProviders() {
            // when
            contentExternalMappingRepository.save(
                ContentExternalProvider.TMDB,
                12345L,
                contentId
            );
            contentExternalMappingRepository.save(
                ContentExternalProvider.TSDB,
                67890L,
                contentId
            );

            // then
            assertThat(contentExternalMappingRepository.exists(
                ContentExternalProvider.TMDB, 12345L
            )).isTrue();
            assertThat(contentExternalMappingRepository.exists(
                ContentExternalProvider.TSDB, 67890L
            )).isTrue();
        }
    }

    @Nested
    @DisplayName("exists()")
    class ExistsTest {

        @Test
        @DisplayName("존재하는 매핑이면 true를 반환한다")
        void withExistingMapping_returnsTrue() {
            // given
            contentExternalMappingRepository.save(
                ContentExternalProvider.TMDB,
                12345L,
                contentId
            );

            // when
            boolean exists = contentExternalMappingRepository.exists(
                ContentExternalProvider.TMDB,
                12345L
            );

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 매핑이면 false를 반환한다")
        void withNonExistingMapping_returnsFalse() {
            // when
            boolean exists = contentExternalMappingRepository.exists(
                ContentExternalProvider.TMDB,
                99999L
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("같은 externalId라도 provider가 다르면 false를 반환한다")
        void withDifferentProvider_returnsFalse() {
            // given
            contentExternalMappingRepository.save(
                ContentExternalProvider.TMDB,
                12345L,
                contentId
            );

            // when
            boolean exists = contentExternalMappingRepository.exists(
                ContentExternalProvider.TSDB,
                12345L
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("같은 provider라도 externalId가 다르면 false를 반환한다")
        void withDifferentExternalId_returnsFalse() {
            // given
            contentExternalMappingRepository.save(
                ContentExternalProvider.TMDB,
                12345L,
                contentId
            );

            // when
            boolean exists = contentExternalMappingRepository.exists(
                ContentExternalProvider.TMDB,
                67890L
            );

            // then
            assertThat(exists).isFalse();
        }
    }
}
