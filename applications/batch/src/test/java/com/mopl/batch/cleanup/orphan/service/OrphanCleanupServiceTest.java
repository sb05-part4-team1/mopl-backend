package com.mopl.batch.cleanup.orphan.service;

import com.mopl.batch.cleanup.orphan.config.OrphanCleanupPolicyResolver;
import com.mopl.batch.cleanup.orphan.config.OrphanCleanupProperties;
import com.mopl.batch.cleanup.orphan.config.OrphanCleanupProperties.PolicyProperties;
import com.mopl.jpa.repository.orphan.JpaOrphanCleanupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrphanCleanupServiceTest {

    @Mock
    private JpaOrphanCleanupRepository orphanCleanupRepository;

    @Mock
    private OrphanCleanupTxService txService;

    @Mock
    private OrphanCleanupProperties props;

    @Mock
    private OrphanCleanupPolicyResolver policyResolver;

    private OrphanCleanupService orphanCleanupService;

    private static final int CHUNK_SIZE = 100;
    private static final long GRACE_PERIOD_DAYS = 7L;

    @BeforeEach
    void setUp() {
        orphanCleanupService = new OrphanCleanupService(
            orphanCleanupRepository,
            txService,
            props,
            policyResolver
        );
    }

    private void setupPolicyResolver(PolicyProperties policy) {
        when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);
        when(policyResolver.gracePeriodDays(policy)).thenReturn(GRACE_PERIOD_DAYS);
    }

    @Nested
    @DisplayName("cleanupConversations")
    class CleanupConversationsTest {

        @Test
        @DisplayName("orphan이 없으면 0을 반환한다")
        void returnsZeroWhenNoOrphans() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, GRACE_PERIOD_DAYS);
            when(props.conversation()).thenReturn(policy);
            setupPolicyResolver(policy);
            when(orphanCleanupRepository.findOrphanConversationIds(any(Instant.class), anyInt()))
                .thenReturn(Collections.emptyList());

            int result = orphanCleanupService.cleanupConversations();

            assertThat(result).isZero();
            verify(txService, never()).cleanupConversations(anyList());
        }

        @Test
        @DisplayName("orphan을 찾아 삭제하고 삭제된 개수를 반환한다")
        void deletesOrphansAndReturnsCount() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, GRACE_PERIOD_DAYS);
            when(props.conversation()).thenReturn(policy);
            setupPolicyResolver(policy);

            List<UUID> orphanIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            when(orphanCleanupRepository.findOrphanConversationIds(any(Instant.class), anyInt()))
                .thenReturn(orphanIds)
                .thenReturn(Collections.emptyList());
            when(txService.cleanupConversations(orphanIds)).thenReturn(2);

            int result = orphanCleanupService.cleanupConversations();

            assertThat(result).isEqualTo(2);
            verify(txService).cleanupConversations(orphanIds);
        }

        @Test
        @DisplayName("여러 배치에 걸쳐 삭제하고 총 삭제 개수를 반환한다")
        void deletesInMultipleBatches() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, GRACE_PERIOD_DAYS);
            when(props.conversation()).thenReturn(policy);
            setupPolicyResolver(policy);

            List<UUID> batch1 = List.of(UUID.randomUUID(), UUID.randomUUID());
            List<UUID> batch2 = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.findOrphanConversationIds(any(Instant.class), anyInt()))
                .thenReturn(batch1)
                .thenReturn(batch2)
                .thenReturn(Collections.emptyList());
            when(txService.cleanupConversations(batch1)).thenReturn(2);
            when(txService.cleanupConversations(batch2)).thenReturn(1);

            int result = orphanCleanupService.cleanupConversations();

            assertThat(result).isEqualTo(3);
            verify(txService, times(2)).cleanupConversations(anyList());
        }

        @Test
        @DisplayName("삭제 결과가 0이면 무한루프 방지를 위해 종료한다")
        void breaksWhenDeleteReturnsZero() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, GRACE_PERIOD_DAYS);
            when(props.conversation()).thenReturn(policy);
            setupPolicyResolver(policy);

            List<UUID> orphanIds = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.findOrphanConversationIds(any(Instant.class), anyInt()))
                .thenReturn(orphanIds);
            when(txService.cleanupConversations(orphanIds)).thenReturn(0);

            int result = orphanCleanupService.cleanupConversations();

            assertThat(result).isZero();
            verify(txService, times(1)).cleanupConversations(anyList());
        }
    }

    @Nested
    @DisplayName("cleanupDirectMessages")
    class CleanupDirectMessagesTest {

        @Test
        @DisplayName("orphan DirectMessage를 삭제한다")
        void deletesOrphanDirectMessages() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, GRACE_PERIOD_DAYS);
            when(props.directMessage()).thenReturn(policy);
            setupPolicyResolver(policy);

            List<UUID> orphanIds = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.findOrphanDirectMessageIds(any(Instant.class), anyInt()))
                .thenReturn(orphanIds)
                .thenReturn(Collections.emptyList());
            when(txService.cleanupDirectMessages(orphanIds)).thenReturn(1);

            int result = orphanCleanupService.cleanupDirectMessages();

            assertThat(result).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("cleanupPlaylists")
    class CleanupPlaylistsTest {

        @Test
        @DisplayName("orphan Playlist를 삭제한다")
        void deletesOrphanPlaylists() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, GRACE_PERIOD_DAYS);
            when(props.playlist()).thenReturn(policy);
            setupPolicyResolver(policy);

            List<UUID> orphanIds = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.findOrphanPlaylistIds(any(Instant.class), anyInt()))
                .thenReturn(orphanIds)
                .thenReturn(Collections.emptyList());
            when(txService.cleanupPlaylists(orphanIds)).thenReturn(1);

            int result = orphanCleanupService.cleanupPlaylists();

            assertThat(result).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("cleanupReviews")
    class CleanupReviewsTest {

        @Test
        @DisplayName("orphan Review를 삭제한다")
        void deletesOrphanReviews() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, GRACE_PERIOD_DAYS);
            when(props.review()).thenReturn(policy);
            setupPolicyResolver(policy);

            List<UUID> orphanIds = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.findOrphanReviewIds(any(Instant.class), anyInt()))
                .thenReturn(orphanIds)
                .thenReturn(Collections.emptyList());
            when(txService.cleanupReviews(orphanIds)).thenReturn(1);

            int result = orphanCleanupService.cleanupReviews();

            assertThat(result).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("cleanupNotifications")
    class CleanupNotificationsTest {

        @Test
        @DisplayName("orphan Notification을 삭제한다")
        void deletesOrphanNotifications() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, GRACE_PERIOD_DAYS);
            when(props.notification()).thenReturn(policy);
            setupPolicyResolver(policy);

            List<UUID> orphanIds = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.findOrphanNotificationIds(any(Instant.class), anyInt()))
                .thenReturn(orphanIds)
                .thenReturn(Collections.emptyList());
            when(txService.cleanupNotifications(orphanIds)).thenReturn(1);

            int result = orphanCleanupService.cleanupNotifications();

            assertThat(result).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("cleanupFollows")
    class CleanupFollowsTest {

        @Test
        @DisplayName("orphan Follow를 삭제한다")
        void deletesOrphanFollows() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, GRACE_PERIOD_DAYS);
            when(props.follow()).thenReturn(policy);
            setupPolicyResolver(policy);

            List<UUID> orphanIds = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.findOrphanFollowIds(any(Instant.class), anyInt()))
                .thenReturn(orphanIds)
                .thenReturn(Collections.emptyList());
            when(txService.cleanupFollows(orphanIds)).thenReturn(1);

            int result = orphanCleanupService.cleanupFollows();

            assertThat(result).isEqualTo(1);
        }
    }
}
