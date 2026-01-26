package com.mopl.batch.cleanup.service.review;

import com.mopl.batch.sync.denormalized.service.ContentReviewStatsSyncTxService;
import com.mopl.jpa.repository.review.JpaReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewCleanupTxService 단위 테스트")
class ReviewCleanupTxServiceTest {

    @Mock
    private JpaReviewRepository jpaReviewRepository;

    @Mock
    private ContentReviewStatsSyncTxService contentReviewStatsSyncTxService;

    @InjectMocks
    private ReviewCleanupTxService reviewCleanupTxService;

    @Nested
    @DisplayName("cleanupBatch()")
    class CleanupBatchTest {

        @Test
        @DisplayName("리뷰 ID 목록을 받아 삭제하고 영향받는 Content 통계를 재계산한다")
        void withReviewIds_deletesAndRecalculatesContentStats() {
            // given
            List<UUID> reviewIds = List.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
            );
            UUID contentId1 = UUID.randomUUID();
            UUID contentId2 = UUID.randomUUID();
            Set<UUID> contentIds = Set.of(contentId1, contentId2);

            given(jpaReviewRepository.findContentIdsByIdIn(reviewIds)).willReturn(contentIds);
            given(jpaReviewRepository.deleteByIdIn(reviewIds)).willReturn(3);

            // when
            int result = reviewCleanupTxService.cleanupBatch(reviewIds);

            // then
            assertThat(result).isEqualTo(3);
            then(jpaReviewRepository).should().findContentIdsByIdIn(reviewIds);
            then(jpaReviewRepository).should().deleteByIdIn(reviewIds);
            then(contentReviewStatsSyncTxService).should().syncOne(contentId1);
            then(contentReviewStatsSyncTxService).should().syncOne(contentId2);
        }

        @Test
        @DisplayName("빈 목록을 받으면 0을 반환하고 통계 재계산을 하지 않는다")
        void withEmptyList_returnsZeroAndNoSync() {
            // given
            List<UUID> emptyList = List.of();

            given(jpaReviewRepository.findContentIdsByIdIn(emptyList)).willReturn(Set.of());
            given(jpaReviewRepository.deleteByIdIn(emptyList)).willReturn(0);

            // when
            int result = reviewCleanupTxService.cleanupBatch(emptyList);

            // then
            assertThat(result).isZero();
            then(contentReviewStatsSyncTxService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("단일 ID를 받아 삭제하고 해당 Content 통계를 재계산한다")
        void withSingleId_deletesAndRecalculatesSingleContentStats() {
            // given
            UUID reviewId = UUID.randomUUID();
            List<UUID> reviewIds = List.of(reviewId);
            UUID contentId = UUID.randomUUID();
            Set<UUID> contentIds = Set.of(contentId);

            given(jpaReviewRepository.findContentIdsByIdIn(reviewIds)).willReturn(contentIds);
            given(jpaReviewRepository.deleteByIdIn(reviewIds)).willReturn(1);

            // when
            int result = reviewCleanupTxService.cleanupBatch(reviewIds);

            // then
            assertThat(result).isEqualTo(1);
            then(contentReviewStatsSyncTxService).should().syncOne(contentId);
        }

        @Test
        @DisplayName("삭제 전 contentId를 먼저 조회한다")
        void fetchesContentIdsBeforeDelete() {
            // given
            List<UUID> reviewIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            UUID contentId = UUID.randomUUID();

            given(jpaReviewRepository.findContentIdsByIdIn(reviewIds)).willReturn(Set.of(contentId));
            given(jpaReviewRepository.deleteByIdIn(reviewIds)).willReturn(2);

            // when
            reviewCleanupTxService.cleanupBatch(reviewIds);

            // then
            var inOrder = org.mockito.Mockito.inOrder(jpaReviewRepository, contentReviewStatsSyncTxService);
            inOrder.verify(jpaReviewRepository).findContentIdsByIdIn(reviewIds);
            inOrder.verify(jpaReviewRepository).deleteByIdIn(reviewIds);
            inOrder.verify(contentReviewStatsSyncTxService).syncOne(contentId);
        }
    }
}
