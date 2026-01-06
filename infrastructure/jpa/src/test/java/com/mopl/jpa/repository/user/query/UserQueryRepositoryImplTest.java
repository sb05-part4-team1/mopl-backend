package com.mopl.jpa.repository.user.query;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.user.UserModel.AuthProvider;
import com.mopl.domain.model.user.UserModel.Role;
import com.mopl.domain.repository.user.UserQueryRepository;
import com.mopl.domain.repository.user.UserQueryRequest;
import com.mopl.domain.repository.user.UserSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.config.QuerydslConfig;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    JpaConfig.class,
    QuerydslConfig.class,
    UserQueryRepositoryImpl.class,
    UserEntityMapper.class
})
@DisplayName("UserQueryRepositoryImpl 슬라이스 테스트")
class UserQueryRepositoryImplTest {

    @Autowired
    private UserQueryRepository userQueryRepository;

    @Autowired
    private EntityManager entityManager;

    private List<UserEntity> testUsers;

    @BeforeEach
    void setUp() {
        testUsers = new ArrayList<>();
        Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        // 다양한 속성을 가진 테스트 사용자 생성
        testUsers.add(createAndPersistUser("alice@example.com", "Alice", Role.USER, false, baseTime));
        testUsers.add(createAndPersistUser("bob@example.com", "Bob", Role.USER, false, baseTime.plusSeconds(1)));
        testUsers.add(createAndPersistUser("charlie@example.com", "Charlie", Role.ADMIN, false, baseTime.plusSeconds(2)));
        testUsers.add(createAndPersistUser("david@example.com", "David", Role.ADMIN, true, baseTime.plusSeconds(3)));
        testUsers.add(createAndPersistUser("eve@test.com", "Eve", Role.USER, true, baseTime.plusSeconds(4)));

        entityManager.flush();
        entityManager.clear();
    }

    private UserEntity createAndPersistUser(String email, String name, Role role, boolean locked, Instant createdAt) {
        UserEntity entity = UserEntity.builder()
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .authProvider(AuthProvider.EMAIL)
            .email(email)
            .name(name)
            .password("encodedPassword")
            .role(role)
            .locked(locked)
            .build();
        entityManager.persist(entity);
        return entity;
    }

    @Nested
    @DisplayName("findAll() - 필터링")
    class FilteringTest {

        @Test
        @DisplayName("필터 없이 전체 조회")
        void withNoFilter_returnsAllUsers() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                null, null, null, null, null, 100, SortDirection.ASCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(5);
            assertThat(response.totalCount()).isEqualTo(5);
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("emailLike로 필터링")
        void withEmailLike_filtersUsers() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                "example.com", null, null, null, null, 100, SortDirection.ASCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(4);
            assertThat(response.data())
                .allMatch(user -> user.getEmail().contains("example.com"));
            assertThat(response.totalCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("roleEqual로 필터링")
        void withRoleEqual_filtersUsers() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                null, Role.ADMIN, null, null, null, 100, SortDirection.ASCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .allMatch(user -> user.getRole() == Role.ADMIN);
            assertThat(response.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("isLocked로 필터링")
        void withIsLocked_filtersUsers() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                null, null, true, null, null, 100, SortDirection.ASCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .allMatch(UserModel::isLocked);
            assertThat(response.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("복합 필터 조합")
        void withMultipleFilters_filtersUsers() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                "example.com", Role.ADMIN, false, null, null, 100, SortDirection.ASCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.data().get(0).getName()).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("조건에 맞는 데이터가 없으면 빈 결과 반환")
        void withNoMatchingData_returnsEmptyResult() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                "nonexistent.com", null, null, null, null, 100, SortDirection.ASCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
            assertThat(response.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("findAll() - 정렬")
    class SortingTest {

        @Test
        @DisplayName("이름으로 오름차순 정렬")
        void sortByNameAscending() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                null, null, null, null, null, 100, SortDirection.ASCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(UserModel::getName)
                .containsExactly("Alice", "Bob", "Charlie", "David", "Eve");
            assertThat(response.sortBy()).isEqualTo("name");
            assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);
        }

        @Test
        @DisplayName("이름으로 내림차순 정렬")
        void sortByNameDescending() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                null, null, null, null, null, 100, SortDirection.DESCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(UserModel::getName)
                .containsExactly("Eve", "David", "Charlie", "Bob", "Alice");
        }

        @Test
        @DisplayName("이메일로 정렬")
        void sortByEmail() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                null, null, null, null, null, 100, SortDirection.ASCENDING, UserSortField.email
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(UserModel::getEmail)
                .containsExactly(
                    "alice@example.com",
                    "bob@example.com",
                    "charlie@example.com",
                    "david@example.com",
                    "eve@test.com"
                );
        }

        @Test
        @DisplayName("생성일시로 정렬")
        void sortByCreatedAt() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                null, null, null, null, null, 100, SortDirection.DESCENDING, UserSortField.createdAt
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(UserModel::getName)
                .containsExactly("Eve", "David", "Charlie", "Bob", "Alice");
        }

        @Test
        @DisplayName("역할로 정렬")
        void sortByRole() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                null, null, null, null, null, 100, SortDirection.ASCENDING, UserSortField.role
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            List<Role> roles = response.data().stream()
                .map(UserModel::getRole)
                .toList();
            assertThat(roles.subList(0, 2)).allMatch(r -> r == Role.ADMIN);
            assertThat(roles.subList(2, 5)).allMatch(r -> r == Role.USER);
        }
    }

    @Nested
    @DisplayName("findAll() - 커서 페이지네이션")
    class PaginationTest {

        @Test
        @DisplayName("첫 페이지 조회 - hasNext=true")
        void firstPage_hasNextIsTrue() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                null, null, null, null, null, 2, SortDirection.ASCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .extracting(UserModel::getName)
                .containsExactly("Alice", "Bob");
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo("Bob");
            assertThat(response.nextIdAfter()).isNotNull();
            assertThat(response.totalCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("커서로 다음 페이지 조회")
        void secondPage_withCursor() {
            // given
            UserQueryRequest firstRequest = new UserQueryRequest(
                null, null, null, null, null, 2, SortDirection.ASCENDING, UserSortField.name
            );
            CursorResponse<UserModel> firstResponse = userQueryRepository.findAll(firstRequest);

            UserQueryRequest secondRequest = new UserQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> secondResponse = userQueryRepository.findAll(secondRequest);

            // then
            assertThat(secondResponse.data()).hasSize(2);
            assertThat(secondResponse.data())
                .extracting(UserModel::getName)
                .containsExactly("Charlie", "David");
            assertThat(secondResponse.hasNext()).isTrue();
        }

        @Test
        @DisplayName("마지막 페이지 조회 - hasNext=false")
        void lastPage_hasNextIsFalse() {
            // given
            UserQueryRequest firstRequest = new UserQueryRequest(
                null, null, null, null, null, 2, SortDirection.ASCENDING, UserSortField.name
            );
            CursorResponse<UserModel> firstResponse = userQueryRepository.findAll(firstRequest);

            UserQueryRequest secondRequest = new UserQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, UserSortField.name
            );
            CursorResponse<UserModel> secondResponse = userQueryRepository.findAll(secondRequest);

            UserQueryRequest thirdRequest = new UserQueryRequest(
                null, null, null,
                secondResponse.nextCursor(),
                secondResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> thirdResponse = userQueryRepository.findAll(thirdRequest);

            // then
            assertThat(thirdResponse.data()).hasSize(1);
            assertThat(thirdResponse.data().get(0).getName()).isEqualTo("Eve");
            assertThat(thirdResponse.hasNext()).isFalse();
            assertThat(thirdResponse.nextCursor()).isNull();
        }

        @Test
        @DisplayName("내림차순 커서 페이지네이션")
        void descendingPagination() {
            // given
            UserQueryRequest firstRequest = new UserQueryRequest(
                null, null, null, null, null, 2, SortDirection.DESCENDING, UserSortField.name
            );
            CursorResponse<UserModel> firstResponse = userQueryRepository.findAll(firstRequest);

            UserQueryRequest secondRequest = new UserQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.DESCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> secondResponse = userQueryRepository.findAll(secondRequest);

            // then
            assertThat(firstResponse.data())
                .extracting(UserModel::getName)
                .containsExactly("Eve", "David");
            assertThat(secondResponse.data())
                .extracting(UserModel::getName)
                .containsExactly("Charlie", "Bob");
        }

        @Test
        @DisplayName("필터와 페이지네이션 조합")
        void paginationWithFilter() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                "example.com", null, null, null, null, 2, SortDirection.ASCENDING, UserSortField.name
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.totalCount()).isEqualTo(4); // example.com 필터에 맞는 총 4명
            assertThat(response.hasNext()).isTrue();
        }

        @Test
        @DisplayName("createdAt 커서 페이지네이션")
        void paginationByCreatedAt() {
            // given
            UserQueryRequest firstRequest = new UserQueryRequest(
                null, null, null, null, null, 2, SortDirection.ASCENDING, UserSortField.createdAt
            );
            CursorResponse<UserModel> firstResponse = userQueryRepository.findAll(firstRequest);

            UserQueryRequest secondRequest = new UserQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, UserSortField.createdAt
            );

            // when
            CursorResponse<UserModel> secondResponse = userQueryRepository.findAll(secondRequest);

            // then
            assertThat(firstResponse.data())
                .extracting(UserModel::getName)
                .containsExactly("Alice", "Bob");
            assertThat(secondResponse.data())
                .extracting(UserModel::getName)
                .containsExactly("Charlie", "David");
        }
    }

    @Nested
    @DisplayName("findAll() - 기본값")
    class DefaultValueTest {

        @Test
        @DisplayName("limit이 null이면 기본값 100 적용")
        void withNullLimit_usesDefaultLimit() {
            // given
            UserQueryRequest request = new UserQueryRequest(
                null, null, null, null, null, null, null, null
            );

            // when
            CursorResponse<UserModel> response = userQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(5);
            assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);
            assertThat(response.sortBy()).isEqualTo("name");
        }
    }
}
