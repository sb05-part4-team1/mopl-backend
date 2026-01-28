package com.mopl.jpa.repository.content.batch;

import com.mopl.domain.repository.content.batch.ContentDeletionLogItem;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.content.ContentDeletionLogEntity;
import com.mopl.jpa.support.batch.JdbcBatchInsertHelper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    ContentDeletionLogRepositoryImpl.class,
    JdbcBatchInsertHelper.class
})
@DisplayName("ContentDeletionLogRepositoryImpl 슬라이스 테스트")
class ContentDeletionLogRepositoryImplTest {

    @Autowired
    private ContentDeletionLogRepository contentDeletionLogRepository;

    @Autowired
    private JpaContentDeletionLogRepository jpaContentDeletionLogRepository;

    @Autowired
    private EntityManager entityManager;

    private ContentDeletionLogEntity createAndSaveLog(UUID contentId, String thumbnailPath) {
        ContentDeletionLogEntity entity = ContentDeletionLogEntity.builder()
            .contentId(contentId)
            .thumbnailPath(thumbnailPath)
            .build();
        return jpaContentDeletionLogRepository.save(entity);
    }

    private ContentDeletionLogEntity createAndSaveProcessedLog(UUID contentId, String thumbnailPath) {
        ContentDeletionLogEntity entity = jpaContentDeletionLogRepository.save(
            ContentDeletionLogEntity.builder()
                .contentId(contentId)
                .thumbnailPath(thumbnailPath)
                .build()
        );
        jpaContentDeletionLogRepository.markImageProcessed(List.of(entity.getId()), Instant.now());
        entityManager.clear();
        return jpaContentDeletionLogRepository.findById(entity.getId()).orElseThrow();
    }

    @Nested
    @DisplayName("saveAll()")
    class SaveAllTest {

        @Test
        @DisplayName("null 입력 시 0 반환")
        void withNull_returnsZero() {
            // when
            int result = contentDeletionLogRepository.saveAll(null);

            // then
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("빈 맵 입력 시 0 반환")
        void withEmptyMap_returnsZero() {
            // when
            int result = contentDeletionLogRepository.saveAll(Map.of());

            // then
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("새 콘텐츠 ID들 저장 시 저장된 개수 반환")
        void withNewContentIds_savesAndReturnsCount() {
            // given
            UUID contentId1 = UUID.randomUUID();
            UUID contentId2 = UUID.randomUUID();
            Map<UUID, String> thumbnailPaths = Map.of(
                contentId1, "path/to/thumbnail1.jpg",
                contentId2, "path/to/thumbnail2.jpg"
            );

            // when
            int result = contentDeletionLogRepository.saveAll(thumbnailPaths);

            // then
            assertThat(result).isEqualTo(2);
            List<UUID> existingIds = jpaContentDeletionLogRepository.findExistingContentIds(
                List.of(contentId1, contentId2)
            );
            assertThat(existingIds).containsExactlyInAnyOrder(contentId1, contentId2);
        }

        @Test
        @DisplayName("이미 존재하는 콘텐츠 ID는 건너뛰고 새 것만 저장")
        void withExistingContentId_skipsExistingAndSavesNew() {
            // given
            UUID existingContentId = UUID.randomUUID();
            UUID newContentId = UUID.randomUUID();
            createAndSaveLog(existingContentId, "existing/path.jpg");

            Map<UUID, String> thumbnailPaths = Map.of(
                existingContentId, "existing/path.jpg",
                newContentId, "new/path.jpg"
            );

            // when
            int result = contentDeletionLogRepository.saveAll(thumbnailPaths);

            // then
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("모든 콘텐츠 ID가 이미 존재하면 0 반환")
        void withAllExistingContentIds_returnsZero() {
            // given
            UUID contentId1 = UUID.randomUUID();
            UUID contentId2 = UUID.randomUUID();
            createAndSaveLog(contentId1, "path1.jpg");
            createAndSaveLog(contentId2, "path2.jpg");

            Map<UUID, String> thumbnailPaths = Map.of(
                contentId1, "path1.jpg",
                contentId2, "path2.jpg"
            );

            // when
            int result = contentDeletionLogRepository.saveAll(thumbnailPaths);

            // then
            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("findImageCleanupTargets()")
    class FindImageCleanupTargetsTest {

        @Test
        @DisplayName("이미지 처리되지 않은 로그 조회")
        void withUnprocessedLogs_returnsLogs() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentDeletionLogEntity entity = createAndSaveLog(contentId, "path/to/image.jpg");

            // when
            List<ContentDeletionLogItem> result = contentDeletionLogRepository.findImageCleanupTargets(10);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().logId()).isEqualTo(entity.getId());
            assertThat(result.getFirst().contentId()).isEqualTo(contentId);
            assertThat(result.getFirst().thumbnailPath()).isEqualTo("path/to/image.jpg");
        }

        @Test
        @DisplayName("이미 처리된 로그는 조회되지 않음")
        void withProcessedLogs_returnsEmpty() {
            // given
            UUID contentId = UUID.randomUUID();
            createAndSaveProcessedLog(contentId, "path/to/image.jpg");

            // when
            List<ContentDeletionLogItem> result = contentDeletionLogRepository.findImageCleanupTargets(10);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("limit만큼만 조회")
        void withLimit_returnsLimitedResults() {
            // given
            for (int i = 0; i < 5; i++) {
                createAndSaveLog(UUID.randomUUID(), "path" + i + ".jpg");
            }

            // when
            List<ContentDeletionLogItem> result = contentDeletionLogRepository.findImageCleanupTargets(3);

            // then
            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("markImageProcessed()")
    class MarkImageProcessedTest {

        @Test
        @DisplayName("null 입력 시 아무 동작 안함")
        void withNull_doesNothing() {
            // when & then (예외 발생하지 않음)
            contentDeletionLogRepository.markImageProcessed(null, Instant.now());
        }

        @Test
        @DisplayName("빈 리스트 입력 시 아무 동작 안함")
        void withEmptyList_doesNothing() {
            // when & then (예외 발생하지 않음)
            contentDeletionLogRepository.markImageProcessed(List.of(), Instant.now());
        }

        @Test
        @DisplayName("로그 ID로 이미지 처리 완료 마킹")
        void withLogIds_marksAsProcessed() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentDeletionLogEntity entity = createAndSaveLog(contentId, "path/to/image.jpg");
            Instant processedAt = Instant.now();

            // when
            contentDeletionLogRepository.markImageProcessed(List.of(entity.getId()), processedAt);
            entityManager.clear();

            // then
            ContentDeletionLogEntity updated = jpaContentDeletionLogRepository.findById(entity.getId())
                .orElseThrow();
            assertThat(updated.getImageProcessedAt()).isEqualTo(processedAt);
        }
    }

    @Nested
    @DisplayName("findFullyProcessedLogIds()")
    class FindFullyProcessedLogIdsTest {

        @Test
        @DisplayName("완전히 처리된 로그 ID 조회")
        void withFullyProcessedLogs_returnsLogIds() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentDeletionLogEntity entity = createAndSaveProcessedLog(contentId, "path/to/image.jpg");

            // when
            List<UUID> result = contentDeletionLogRepository.findFullyProcessedLogIds(10);

            // then
            assertThat(result).containsExactly(entity.getId());
        }

        @Test
        @DisplayName("처리되지 않은 로그는 조회되지 않음")
        void withUnprocessedLogs_returnsEmpty() {
            // given
            UUID contentId = UUID.randomUUID();
            createAndSaveLog(contentId, "path/to/image.jpg");

            // when
            List<UUID> result = contentDeletionLogRepository.findFullyProcessedLogIds(10);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("limit만큼만 조회")
        void withLimit_returnsLimitedResults() {
            // given
            for (int i = 0; i < 5; i++) {
                createAndSaveProcessedLog(UUID.randomUUID(), "path" + i + ".jpg");
            }

            // when
            List<UUID> result = contentDeletionLogRepository.findFullyProcessedLogIds(3);

            // then
            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("deleteByIdIn()")
    class DeleteByIdInTest {

        @Test
        @DisplayName("null 입력 시 0 반환")
        void withNull_returnsZero() {
            // when
            int result = contentDeletionLogRepository.deleteByIdIn(null);

            // then
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("빈 리스트 입력 시 0 반환")
        void withEmptyList_returnsZero() {
            // when
            int result = contentDeletionLogRepository.deleteByIdIn(List.of());

            // then
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("로그 ID로 삭제 시 삭제된 개수 반환")
        void withLogIds_deletesAndReturnsCount() {
            // given
            UUID contentId1 = UUID.randomUUID();
            UUID contentId2 = UUID.randomUUID();
            ContentDeletionLogEntity entity1 = createAndSaveLog(contentId1, "path1.jpg");
            ContentDeletionLogEntity entity2 = createAndSaveLog(contentId2, "path2.jpg");

            // when
            int result = contentDeletionLogRepository.deleteByIdIn(
                List.of(entity1.getId(), entity2.getId())
            );
            entityManager.clear();

            // then
            assertThat(result).isEqualTo(2);
            assertThat(jpaContentDeletionLogRepository.findById(entity1.getId())).isEmpty();
            assertThat(jpaContentDeletionLogRepository.findById(entity2.getId())).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 ID 삭제 시 0 반환")
        void withNonExistingIds_returnsZero() {
            // when
            int result = contentDeletionLogRepository.deleteByIdIn(List.of(UUID.randomUUID()));

            // then
            assertThat(result).isZero();
        }
    }
}
