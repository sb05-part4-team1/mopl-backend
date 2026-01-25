package com.mopl.batch.cleanup.service.review;

import com.mopl.batch.cleanup.properties.CleanupPolicyProperties;
import com.mopl.batch.cleanup.properties.CleanupPolicyResolver;
import com.mopl.batch.cleanup.properties.CleanupProperties;
import com.mopl.domain.repository.review.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewCleanupService 단위 테스트")
class ReviewCleanupServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewCleanupTxService executor;

    @Mock
    private CleanupProperties cleanupProperties;

    @Mock
    private CleanupPolicyResolver policyResolver;

    private ReviewCleanupService reviewCleanupService;

    @BeforeEach
    void setUp() {
        reviewCleanupService = new ReviewCleanupService(
            reviewRepository,
            executor,
            cleanupProperties,
            policyResolver
        );
    }

    @Nested
    @DisplayName("cleanup()")
    class CleanupTest {

        @Test
        @DisplayName("삭제할 리뷰가 없으면 0을 반환하고 executor를 호출하지 않는다")
        void withNoReviewsToDelete_returnsZeroAndDoesNotCallExecutor() {
            // given
            CleanupPolicyProperties reviewPolicy = new CleanupPolicyProperties();
            given(cleanupProperties.getReview()).willReturn(reviewPolicy);
            given(policyResolver.chunkSize(reviewPolicy)).willReturn(100);
            given(policyResolver.retentionDaysRequired(reviewPolicy)).willReturn(30L);
            given(reviewRepository.findCleanupTargets(any(Instant.class), eq(100)))
                .willReturn(List.of());

            // when
            int result = reviewCleanupService.cleanup();

            // then
            assertThat(result).isZero();
            then(executor).should(never()).cleanupBatch(any());
        }

        @Test
        @DisplayName("한 청크 내에서 모든 리뷰를 삭제하면 해당 개수를 반환한다")
        void withSingleChunk_deletesAndReturnsCount() {
            // given
            CleanupPolicyProperties reviewPolicy = new CleanupPolicyProperties();
            given(cleanupProperties.getReview()).willReturn(reviewPolicy);
            given(policyResolver.chunkSize(reviewPolicy)).willReturn(100);
            given(policyResolver.retentionDaysRequired(reviewPolicy)).willReturn(30L);

            List<UUID> reviewIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            given(reviewRepository.findCleanupTargets(any(Instant.class), eq(100)))
                .willReturn(reviewIds)
                .willReturn(List.of());

            given(executor.cleanupBatch(reviewIds)).willReturn(3);

            // when
            int result = reviewCleanupService.cleanup();

            // then
            assertThat(result).isEqualTo(3);
            then(executor).should(times(1)).cleanupBatch(reviewIds);
        }

        @Test
        @DisplayName("여러 청크에 걸쳐 리뷰를 삭제하면 총 삭제 개수를 반환한다")
        void withMultipleChunks_deletesAllAndReturnsTotalCount() {
            // given
            CleanupPolicyProperties reviewPolicy = new CleanupPolicyProperties();
            given(cleanupProperties.getReview()).willReturn(reviewPolicy);
            given(policyResolver.chunkSize(reviewPolicy)).willReturn(2);
            given(policyResolver.retentionDaysRequired(reviewPolicy)).willReturn(30L);

            List<UUID> firstBatch = List.of(UUID.randomUUID(), UUID.randomUUID());
            List<UUID> secondBatch = List.of(UUID.randomUUID(), UUID.randomUUID());
            List<UUID> thirdBatch = List.of(UUID.randomUUID());

            given(reviewRepository.findCleanupTargets(any(Instant.class), eq(2)))
                .willReturn(firstBatch)
                .willReturn(secondBatch)
                .willReturn(thirdBatch)
                .willReturn(List.of());

            given(executor.cleanupBatch(firstBatch)).willReturn(2);
            given(executor.cleanupBatch(secondBatch)).willReturn(2);
            given(executor.cleanupBatch(thirdBatch)).willReturn(1);

            // when
            int result = reviewCleanupService.cleanup();

            // then
            assertThat(result).isEqualTo(5);
            then(executor).should(times(3)).cleanupBatch(any());
        }

        @Test
        @DisplayName("설정된 chunkSize와 retentionDays가 적용된다")
        void appliesConfiguredChunkSizeAndRetentionDays() {
            // given
            CleanupPolicyProperties reviewPolicy = new CleanupPolicyProperties();
            given(cleanupProperties.getReview()).willReturn(reviewPolicy);
            given(policyResolver.chunkSize(reviewPolicy)).willReturn(50);
            given(policyResolver.retentionDaysRequired(reviewPolicy)).willReturn(7L);

            given(reviewRepository.findCleanupTargets(any(Instant.class), eq(50)))
                .willReturn(List.of());

            // when
            reviewCleanupService.cleanup();

            // then
            then(policyResolver).should().chunkSize(reviewPolicy);
            then(policyResolver).should().retentionDaysRequired(reviewPolicy);
            then(reviewRepository).should().findCleanupTargets(any(Instant.class), eq(50));
        }
    }
}
