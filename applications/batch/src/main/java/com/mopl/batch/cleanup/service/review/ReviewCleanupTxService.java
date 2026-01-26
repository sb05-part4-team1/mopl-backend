package com.mopl.batch.cleanup.service.review;

import com.mopl.batch.sync.denormalized.service.ContentReviewStatsSyncTxService;
import com.mopl.jpa.repository.review.JpaReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewCleanupTxService {

    private final JpaReviewRepository jpaReviewRepository;
    private final ContentReviewStatsSyncTxService contentReviewStatsSyncTxService;

    @Transactional
    public int cleanupBatch(List<UUID> reviewIds) {
        // 1. 삭제 전 영향받는 contentId 조회
        Set<UUID> contentIds = jpaReviewRepository.findContentIdsByIdIn(reviewIds);

        // 2. Review hard delete
        int deleted = jpaReviewRepository.deleteByIdIn(reviewIds);

        // 3. Content 통계 재계산
        contentIds.forEach(contentReviewStatsSyncTxService::syncOne);

        return deleted;
    }
}
