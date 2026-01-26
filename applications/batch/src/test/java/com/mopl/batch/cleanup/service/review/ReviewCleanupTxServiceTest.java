package com.mopl.batch.cleanup.service.review;

import com.mopl.domain.repository.review.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewCleanupTxService 단위 테스트")
class ReviewCleanupTxServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewCleanupTxService reviewCleanupTxService;

    @Nested
    @DisplayName("cleanupBatch()")
    class CleanupBatchTest {

        @Test
        @DisplayName("리뷰 ID 목록을 받아 삭제하고 삭제된 개수를 반환한다")
        void withReviewIds_deletesAndReturnsCount() {
            // given
            List<UUID> reviewIds = List.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
            );

            given(reviewRepository.deleteByIdIn(reviewIds)).willReturn(3);

            // when
            int result = reviewCleanupTxService.cleanupBatch(reviewIds);

            // then
            assertThat(result).isEqualTo(3);
            then(reviewRepository).should().deleteByIdIn(reviewIds);
        }

        @Test
        @DisplayName("빈 목록을 받으면 0을 반환한다")
        void withEmptyList_returnsZero() {
            // given
            List<UUID> emptyList = List.of();

            given(reviewRepository.deleteByIdIn(emptyList)).willReturn(0);

            // when
            int result = reviewCleanupTxService.cleanupBatch(emptyList);

            // then
            assertThat(result).isZero();
            then(reviewRepository).should().deleteByIdIn(emptyList);
        }

        @Test
        @DisplayName("단일 ID를 받아 삭제하고 1을 반환한다")
        void withSingleId_deletesAndReturnsOne() {
            // given
            UUID reviewId = UUID.randomUUID();
            List<UUID> reviewIds = List.of(reviewId);

            given(reviewRepository.deleteByIdIn(reviewIds)).willReturn(1);

            // when
            int result = reviewCleanupTxService.cleanupBatch(reviewIds);

            // then
            assertThat(result).isEqualTo(1);
            then(reviewRepository).should().deleteByIdIn(reviewIds);
        }

        @Test
        @DisplayName("repository에 위임하여 삭제를 수행한다")
        void delegatesToRepository() {
            // given
            List<UUID> reviewIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            given(reviewRepository.deleteByIdIn(reviewIds)).willReturn(2);

            // when
            reviewCleanupTxService.cleanupBatch(reviewIds);

            // then
            then(reviewRepository).should().deleteByIdIn(reviewIds);
        }
    }
}
