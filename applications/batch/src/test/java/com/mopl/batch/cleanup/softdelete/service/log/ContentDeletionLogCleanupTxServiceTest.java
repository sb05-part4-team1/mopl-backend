package com.mopl.batch.cleanup.softdelete.service.log;

import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentDeletionLogCleanupTxService 단위 테스트")
class ContentDeletionLogCleanupTxServiceTest {

    @Mock
    private ContentDeletionLogRepository contentDeletionLogRepository;

    private ContentDeletionLogCleanupTxService txService;

    @BeforeEach
    void setUp() {
        txService = new ContentDeletionLogCleanupTxService(contentDeletionLogRepository);
    }

    @Nested
    @DisplayName("cleanupBatch()")
    class CleanupBatchTest {

        @Test
        @DisplayName("로그를 삭제하고 삭제된 개수를 반환한다")
        void deletesLogsAndReturnsCount() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<UUID> logIds = List.of(id1, id2);

            when(contentDeletionLogRepository.deleteByIdIn(logIds)).thenReturn(2);

            int result = txService.cleanupBatch(logIds);

            assertThat(result).isEqualTo(2);
            verify(contentDeletionLogRepository).deleteByIdIn(logIds);
        }

        @Test
        @DisplayName("삭제할 로그가 없으면 0을 반환한다")
        void returnsZeroWhenNoLogs() {
            List<UUID> logIds = List.of(UUID.randomUUID());

            when(contentDeletionLogRepository.deleteByIdIn(logIds)).thenReturn(0);

            int result = txService.cleanupBatch(logIds);

            assertThat(result).isZero();
        }
    }
}
