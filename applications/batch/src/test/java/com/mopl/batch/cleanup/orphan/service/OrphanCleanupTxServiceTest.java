package com.mopl.batch.cleanup.orphan.service;

import com.mopl.batch.sync.denormalized.service.ContentReviewStatsSyncTxService;
import com.mopl.batch.sync.denormalized.service.PlaylistSubscriberCountSyncTxService;
import com.mopl.jpa.repository.orphan.JpaOrphanCleanupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrphanCleanupTxServiceTest {

    @Mock
    private JpaOrphanCleanupRepository orphanCleanupRepository;

    @Mock
    private ContentReviewStatsSyncTxService contentReviewStatsSyncTxService;

    @Mock
    private PlaylistSubscriberCountSyncTxService playlistSubscriberCountSyncTxService;

    private OrphanCleanupTxService txService;

    @BeforeEach
    void setUp() {
        txService = new OrphanCleanupTxService(
            orphanCleanupRepository,
            contentReviewStatsSyncTxService,
            playlistSubscriberCountSyncTxService
        );
    }

    @Nested
    @DisplayName("cleanupConversations")
    class CleanupConversationsTest {

        @Test
        @DisplayName("Conversation 삭제 시 관련 DirectMessage도 함께 삭제한다")
        void deletesConversationsWithDirectMessages() {
            List<UUID> conversationIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            when(orphanCleanupRepository.deleteConversationsByIdIn(conversationIds)).thenReturn(2);

            int result = txService.cleanupConversations(conversationIds);

            assertThat(result).isEqualTo(2);
            verify(orphanCleanupRepository).deleteDirectMessagesByConversationIdIn(conversationIds);
            verify(orphanCleanupRepository).deleteConversationsByIdIn(conversationIds);
        }
    }

    @Nested
    @DisplayName("cleanupPlaylists")
    class CleanupPlaylistsTest {

        @Test
        @DisplayName("Playlist 삭제 시 관련 Content와 Subscriber도 함께 삭제한다")
        void deletesPlaylistsWithRelated() {
            List<UUID> playlistIds = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.deletePlaylistsByIdIn(playlistIds)).thenReturn(1);

            int result = txService.cleanupPlaylists(playlistIds);

            assertThat(result).isEqualTo(1);
            verify(orphanCleanupRepository).deletePlaylistContentsByPlaylistIdIn(playlistIds);
            verify(orphanCleanupRepository).deletePlaylistSubscribersByPlaylistIdIn(playlistIds);
            verify(orphanCleanupRepository).deletePlaylistsByIdIn(playlistIds);
        }
    }

    @Nested
    @DisplayName("cleanupPlaylistSubscribers")
    class CleanupPlaylistSubscribersTest {

        @Test
        @DisplayName("PlaylistSubscriber 삭제 후 관련 Playlist의 구독자 수를 동기화한다")
        void deletesAndSyncsSubscriberCount() {
            UUID playlistId = UUID.randomUUID();
            List<UUID> subscriberIds = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.findExistingPlaylistIdsBySubscriberIdIn(subscriberIds))
                .thenReturn(Set.of(playlistId));
            when(orphanCleanupRepository.deletePlaylistSubscribersByIdIn(subscriberIds)).thenReturn(1);

            int result = txService.cleanupPlaylistSubscribers(subscriberIds);

            assertThat(result).isEqualTo(1);
            verify(playlistSubscriberCountSyncTxService).syncOne(playlistId);
        }
    }

    @Nested
    @DisplayName("cleanupReviews")
    class CleanupReviewsTest {

        @Test
        @DisplayName("Review 삭제 후 관련 Content의 리뷰 통계를 동기화한다")
        void deletesAndSyncsReviewStats() {
            UUID contentId = UUID.randomUUID();
            List<UUID> reviewIds = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.findExistingContentIdsByReviewIdIn(reviewIds))
                .thenReturn(Set.of(contentId));
            when(orphanCleanupRepository.deleteReviewsByIdIn(reviewIds)).thenReturn(1);

            int result = txService.cleanupReviews(reviewIds);

            assertThat(result).isEqualTo(1);
            verify(contentReviewStatsSyncTxService).syncOne(contentId);
        }
    }

    @Nested
    @DisplayName("단순 삭제 작업")
    class SimpleDeleteTest {

        @Test
        @DisplayName("DirectMessage를 삭제한다")
        void deletesDirectMessages() {
            List<UUID> ids = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.deleteDirectMessagesByIdIn(ids)).thenReturn(1);

            int result = txService.cleanupDirectMessages(ids);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("PlaylistContent를 삭제한다")
        void deletesPlaylistContents() {
            List<UUID> ids = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.deletePlaylistContentsByIdIn(ids)).thenReturn(1);

            int result = txService.cleanupPlaylistContents(ids);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("ContentTag를 삭제한다")
        void deletesContentTags() {
            List<UUID> ids = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.deleteContentTagsByIdIn(ids)).thenReturn(1);

            int result = txService.cleanupContentTags(ids);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("ContentExternalMapping을 삭제한다")
        void deletesContentExternalMappings() {
            List<UUID> ids = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.deleteContentExternalMappingsByIdIn(ids)).thenReturn(1);

            int result = txService.cleanupContentExternalMappings(ids);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("Notification을 삭제한다")
        void deletesNotifications() {
            List<UUID> ids = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.deleteNotificationsByIdIn(ids)).thenReturn(1);

            int result = txService.cleanupNotifications(ids);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("Follow를 삭제한다")
        void deletesFollows() {
            List<UUID> ids = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.deleteFollowsByIdIn(ids)).thenReturn(1);

            int result = txService.cleanupFollows(ids);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("ReadStatus를 삭제한다")
        void deletesReadStatuses() {
            List<UUID> ids = List.of(UUID.randomUUID());
            when(orphanCleanupRepository.deleteReadStatusesByIdIn(ids)).thenReturn(1);

            int result = txService.cleanupReadStatuses(ids);

            assertThat(result).isEqualTo(1);
        }
    }
}
