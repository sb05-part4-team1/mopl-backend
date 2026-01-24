package com.mopl.jpa.repository.playlist.query;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistQueryRepository;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.repository.playlist.PlaylistSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.config.QuerydslConfig;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.entity.playlist.PlaylistEntityMapper;
import com.mopl.jpa.entity.playlist.PlaylistSubscriberEntity;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
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
    QuerydslConfig.class,
    PlaylistQueryRepositoryImpl.class,
    PlaylistEntityMapper.class,
    UserEntityMapper.class
})
@DisplayName("PlaylistQueryRepositoryImpl 슬라이스 테스트")
class PlaylistQueryRepositoryImplTest {

    @Autowired
    private PlaylistQueryRepository playlistQueryRepository;

    @Autowired
    private EntityManager entityManager;

    private UserEntity owner1;
    private UserEntity owner2;
    private UserEntity subscriber1;

    @BeforeEach
    void setUp() {
        Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        owner1 = createAndPersistUser("owner1@example.com", "Owner1", baseTime);
        owner2 = createAndPersistUser("owner2@example.com", "Owner2", baseTime);
        subscriber1 = createAndPersistUser("subscriber1@example.com", "Subscriber1", baseTime);

        // playlist1: owner1, updatedAt=baseTime, subscriberCount=3
        PlaylistEntity playlist1 = createAndPersistPlaylist("음악 모음", "좋아하는 음악", owner1, baseTime, 3);

        // playlist2: owner1, updatedAt=baseTime+1, subscriberCount=1
        PlaylistEntity playlist2 = createAndPersistPlaylist("영화 OST", "영화 음악 모음", owner1, baseTime.plusSeconds(1), 1);

        // playlist3: owner2, updatedAt=baseTime+2, subscriberCount=2
        PlaylistEntity playlist3 = createAndPersistPlaylist("힐링 음악", "힐링되는 플리", owner2, baseTime.plusSeconds(2), 2);

        // playlist4: owner2, updatedAt=baseTime+3, subscriberCount=0
        createAndPersistPlaylist("운동 음악", "운동할 때 듣는 플리", owner2, baseTime.plusSeconds(3), 0);

        // subscriber 설정 (subscriberIdEqual 필터 테스트용으로 여전히 필요)
        createAndPersistSubscription(playlist1, owner1);
        createAndPersistSubscription(playlist1, owner2);
        createAndPersistSubscription(playlist1, subscriber1);

        createAndPersistSubscription(playlist2, subscriber1);

        createAndPersistSubscription(playlist3, owner1);
        createAndPersistSubscription(playlist3, subscriber1);

        entityManager.flush();
        entityManager.clear();
    }

    private UserEntity createAndPersistUser(String email, String name, Instant createdAt) {
        UserEntity entity = UserEntity.builder()
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .authProvider(UserModel.AuthProvider.EMAIL)
            .email(email)
            .name(name)
            .password("encodedPassword")
            .role(UserModel.Role.USER)
            .locked(false)
            .build();
        entityManager.persist(entity);
        return entity;
    }

    private PlaylistEntity createAndPersistPlaylist(
        String title,
        String description,
        UserEntity owner,
        Instant updatedAt,
        int subscriberCount
    ) {
        PlaylistEntity entity = PlaylistEntity.builder()
            .createdAt(updatedAt)
            .updatedAt(updatedAt)
            .owner(owner)
            .title(title)
            .description(description)
            .subscriberCount(subscriberCount)
            .build();
        entityManager.persist(entity);
        return entity;
    }

    private void createAndPersistSubscription(PlaylistEntity playlist, UserEntity subscriber) {
        PlaylistSubscriberEntity entity = PlaylistSubscriberEntity.builder()
            .createdAt(Instant.now())
            .playlist(playlist)
            .subscriber(subscriber)
            .build();
        entityManager.persist(entity);
    }

    @Nested
    @DisplayName("findAll() - 필터링")
    class FilteringTest {

        @Test
        @DisplayName("필터 없이 전체 조회")
        void withNoFilter_returnsAllPlaylists() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(4);
            assertThat(response.totalCount()).isEqualTo(4);
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("keywordLike로 title 필터링")
        void withKeywordLikeTitle_filtersPlaylists() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                "모음", null, null, null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .allMatch(p -> p.getTitle().contains("모음") || p.getDescription().contains("모음"));
            assertThat(response.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("keywordLike로 description 필터링")
        void withKeywordLikeDescription_filtersPlaylists() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                "힐링", null, null, null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.data().getFirst().getTitle()).isEqualTo("힐링 음악");
        }

        @Test
        @DisplayName("ownerIdEqual로 필터링")
        void withOwnerIdEqual_filtersPlaylists() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, owner1.getId(), null, null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .allMatch(p -> p.getOwner().getId().equals(owner1.getId()));
            assertThat(response.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("subscriberIdEqual로 필터링")
        void withSubscriberIdEqual_filtersPlaylists() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, subscriber1.getId(), null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(3);
            assertThat(response.totalCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("복합 필터 조합")
        void withMultipleFilters_filtersPlaylists() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                "음악", owner2.getId(), null, null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .allMatch(p -> p.getTitle().contains("음악") && p.getOwner().getId().equals(owner2
                    .getId()));
        }

        @Test
        @DisplayName("조건에 맞는 데이터가 없으면 빈 결과 반환")
        void withNoMatchingData_returnsEmptyResult() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                "존재하지않는키워드", null, null, null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
            assertThat(response.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("findAll() - updatedAt 정렬")
    class SortingByUpdatedAtTest {

        @Test
        @DisplayName("updatedAt 오름차순 정렬")
        void sortByUpdatedAtAscending() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("음악 모음", "영화 OST", "힐링 음악", "운동 음악");
            assertThat(response.sortBy()).isEqualTo("updatedAt");
            assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);
        }

        @Test
        @DisplayName("updatedAt 내림차순 정렬")
        void sortByUpdatedAtDescending() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 100, SortDirection.DESCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("운동 음악", "힐링 음악", "영화 OST", "음악 모음");
        }
    }

    @Nested
    @DisplayName("findAll() - subscribeCount 정렬")
    class SortingBySubscribeCountTest {

        @Test
        @DisplayName("subscribeCount 오름차순 정렬")
        void sortBySubscribeCountAscending() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.subscribeCount
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            // 0, 1, 2, 3 순서: 운동 음악(0), 영화 OST(1), 힐링 음악(2), 음악 모음(3)
            assertThat(response.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("운동 음악", "영화 OST", "힐링 음악", "음악 모음");
            assertThat(response.sortBy()).isEqualTo("subscribeCount");
        }

        @Test
        @DisplayName("subscribeCount 내림차순 정렬")
        void sortBySubscribeCountDescending() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 100, SortDirection.DESCENDING,
                PlaylistSortField.subscribeCount
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            // 3, 2, 1, 0 순서: 음악 모음(3), 힐링 음악(2), 영화 OST(1), 운동 음악(0)
            assertThat(response.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("음악 모음", "힐링 음악", "영화 OST", "운동 음악");
        }
    }

    @Nested
    @DisplayName("findAll() - updatedAt 커서 페이지네이션")
    class PaginationByUpdatedAtTest {

        @Test
        @DisplayName("첫 페이지 조회 - hasNext=true")
        void firstPage_hasNextIsTrue() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 2, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("음악 모음", "영화 OST");
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();
            assertThat(response.nextIdAfter()).isNotNull();
            assertThat(response.totalCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("커서로 다음 페이지 조회")
        void secondPage_withCursor() {
            // given
            PlaylistQueryRequest firstRequest = new PlaylistQueryRequest(
                null, null, null, null, null, 2, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );
            CursorResponse<PlaylistModel> firstResponse = playlistQueryRepository.findAll(
                firstRequest);

            PlaylistQueryRequest secondRequest = new PlaylistQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> secondResponse = playlistQueryRepository.findAll(
                secondRequest);

            // then
            assertThat(secondResponse.data()).hasSize(2);
            assertThat(secondResponse.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("힐링 음악", "운동 음악");
            assertThat(secondResponse.hasNext()).isFalse();
        }

        @Test
        @DisplayName("내림차순 커서 페이지네이션")
        void descendingPagination() {
            // given
            PlaylistQueryRequest firstRequest = new PlaylistQueryRequest(
                null, null, null, null, null, 2, SortDirection.DESCENDING,
                PlaylistSortField.updatedAt
            );
            CursorResponse<PlaylistModel> firstResponse = playlistQueryRepository.findAll(
                firstRequest);

            PlaylistQueryRequest secondRequest = new PlaylistQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.DESCENDING, PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> secondResponse = playlistQueryRepository.findAll(
                secondRequest);

            // then
            assertThat(firstResponse.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("운동 음악", "힐링 음악");
            assertThat(secondResponse.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("영화 OST", "음악 모음");
        }
    }

    @Nested
    @DisplayName("findAll() - subscribeCount 커서 페이지네이션")
    class PaginationBySubscribeCountTest {

        @Test
        @DisplayName("subscribeCount 오름차순 첫 페이지")
        void ascendingFirstPage() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 2, SortDirection.ASCENDING,
                PlaylistSortField.subscribeCount
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("운동 음악", "영화 OST");
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo("1");
        }

        @Test
        @DisplayName("subscribeCount 오름차순 다음 페이지")
        void ascendingSecondPage() {
            // given
            PlaylistQueryRequest firstRequest = new PlaylistQueryRequest(
                null, null, null, null, null, 2, SortDirection.ASCENDING,
                PlaylistSortField.subscribeCount
            );
            CursorResponse<PlaylistModel> firstResponse = playlistQueryRepository.findAll(
                firstRequest);

            PlaylistQueryRequest secondRequest = new PlaylistQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, PlaylistSortField.subscribeCount
            );

            // when
            CursorResponse<PlaylistModel> secondResponse = playlistQueryRepository.findAll(
                secondRequest);

            // then
            assertThat(secondResponse.data()).hasSize(2);
            assertThat(secondResponse.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("힐링 음악", "음악 모음");
            assertThat(secondResponse.hasNext()).isFalse();
        }

        @Test
        @DisplayName("subscribeCount 내림차순 페이지네이션")
        void descendingPagination() {
            // given
            PlaylistQueryRequest firstRequest = new PlaylistQueryRequest(
                null, null, null, null, null, 2, SortDirection.DESCENDING,
                PlaylistSortField.subscribeCount
            );
            CursorResponse<PlaylistModel> firstResponse = playlistQueryRepository.findAll(
                firstRequest);

            PlaylistQueryRequest secondRequest = new PlaylistQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.DESCENDING, PlaylistSortField.subscribeCount
            );

            // when
            CursorResponse<PlaylistModel> secondResponse = playlistQueryRepository.findAll(
                secondRequest);

            // then
            assertThat(firstResponse.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("음악 모음", "힐링 음악");
            assertThat(secondResponse.data())
                .extracting(PlaylistModel::getTitle)
                .containsExactly("영화 OST", "운동 음악");
        }
    }

    @Nested
    @DisplayName("findAll() - Owner 정보 포함")
    class OwnerInfoTest {

        @Test
        @DisplayName("조회 결과에 Owner 정보가 포함된다")
        void withResult_includesOwnerInfo() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, owner1.getId(), null, null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).isNotEmpty();
            assertThat(response.data()).allSatisfy(playlist -> {
                assertThat(playlist.getOwner()).isNotNull();
                assertThat(playlist.getOwner().getId()).isEqualTo(owner1.getId());
                assertThat(playlist.getOwner().getName()).isEqualTo("Owner1");
                assertThat(playlist.getOwner().getEmail()).isEqualTo("owner1@example.com");
            });
        }
    }

    @Nested
    @DisplayName("findAll() - 기본값")
    class DefaultValueTest {

        @Test
        @DisplayName("limit이 null이면 기본값 100 적용")
        void withNullLimit_usesDefaultLimit() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, null, null, null
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(4);
            assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);
            assertThat(response.sortBy()).isEqualTo("updatedAt");
        }
    }

    @Nested
    @DisplayName("findAll() - 필터와 페이지네이션 조합")
    class FilterAndPaginationTest {

        @Test
        @DisplayName("필터와 updatedAt 페이지네이션 조합")
        void withFilterAndUpdatedAtPagination() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                "OST", null, null, null, null, 1, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.totalCount()).isEqualTo(1);
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("필터와 subscribeCount 페이지네이션 조합")
        void withFilterAndSubscribeCountPagination() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, owner2.getId(), null, null, null, 1, SortDirection.DESCENDING,
                PlaylistSortField.subscribeCount
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.data().getFirst().getTitle()).isEqualTo("힐링 음악"); // subscribeCount=2
            assertThat(response.totalCount()).isEqualTo(2);
            assertThat(response.hasNext()).isTrue();
        }
    }

    @Nested
    @DisplayName("findAll() - 존재하지 않는 ID 필터")
    class NonExistingIdFilterTest {

        @Test
        @DisplayName("존재하지 않는 ownerId로 필터링하면 빈 결과")
        void withNonExistingOwnerId_returnsEmpty() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, UUID.randomUUID(), null, null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
        }

        @Test
        @DisplayName("존재하지 않는 subscriberId로 필터링하면 빈 결과")
        void withNonExistingSubscriberId_returnsEmpty() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, UUID.randomUUID(), null, null, 100, SortDirection.ASCENDING,
                PlaylistSortField.updatedAt
            );

            // when
            CursorResponse<PlaylistModel> response = playlistQueryRepository.findAll(request);

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
        }
    }
}
