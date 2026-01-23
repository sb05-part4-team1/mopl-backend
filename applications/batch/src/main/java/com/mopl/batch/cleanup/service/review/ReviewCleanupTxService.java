package com.mopl.batch.cleanup.service.review;

import com.mopl.domain.repository.review.ReviewRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewCleanupTxService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public int cleanupBatch(List<UUID> reviewIds) {
        return reviewRepository.deleteAllByIds(reviewIds);
    }
}
