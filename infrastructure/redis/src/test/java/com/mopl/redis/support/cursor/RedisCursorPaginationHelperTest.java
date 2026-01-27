package com.mopl.redis.support.cursor;

import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RedisCursorPaginationHelper 단위 테스트")
class RedisCursorPaginationHelperTest {

    enum TestSortField {
        CREATED_AT
    }

    static class TestSortFieldImpl implements RedisSortField<Instant> {

        @Override
        public String serializeCursor(Instant value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public Instant deserializeCursor(String cursor) {
            if (cursor == null || cursor.isBlank()) {
                return null;
            }
            try {
                return Instant.parse(cursor.trim());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String getFieldName() {
            return "CREATED_AT";
        }
    }

    record TestItem(UUID id, Instant createdAt, String name) {
    }

    private final RedisSortField<Instant> sortField = new TestSortFieldImpl();

    private CursorRequest<TestSortField> createRequest(int limit) {
        return createRequest(null, null, limit, SortDirection.ASCENDING);
    }

    private CursorRequest<TestSortField> createRequest(
        String cursor, UUID idAfter, int limit, SortDirection direction
    ) {
        return new CursorRequest<>() {

            @Override
            public String cursor() {
                return cursor;
            }

            @Override
            public UUID idAfter() {
                return idAfter;
            }

            @Override
            public Integer limit() {
                return limit;
            }

            @Override
            public SortDirection sortDirection() {
                return direction;
            }

            @Override
            public TestSortField sortBy() {
                return TestSortField.CREATED_AT;
            }
        };
    }

    @Nested
    @DisplayName("applyCursor()")
    class ApplyCursorTest {

        @Test
        @DisplayName("cursor와 idAfter가 null이면 원본 리스트 반환")
        void withNullCursorAndIdAfter_returnsOriginalList() {
            // given
            CursorRequest<TestSortField> request = createRequest(10);
            List<TestItem> items = List.of(
                new TestItem(UUID.randomUUID(), Instant.now(), "Item1"),
                new TestItem(UUID.randomUUID(), Instant.now(), "Item2")
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(items);
        }

        @Test
        @DisplayName("cursor만 있고 idAfter가 null이면 원본 리스트 반환")
        void withCursorButNullIdAfter_returnsOriginalList() {
            // given
            CursorRequest<TestSortField> request = createRequest(
                Instant.now().toString(), null, 10, SortDirection.ASCENDING
            );
            List<TestItem> items = List.of(
                new TestItem(UUID.randomUUID(), Instant.now(), "Item1")
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(result).isEqualTo(items);
        }

        @Test
        @DisplayName("idAfter만 있고 cursor가 빈 문자열이면 원본 리스트 반환")
        void withIdAfterButEmptyCursor_returnsOriginalList() {
            // given
            CursorRequest<TestSortField> request = createRequest(
                "", UUID.randomUUID(), 10, SortDirection.ASCENDING
            );
            List<TestItem> items = List.of(
                new TestItem(UUID.randomUUID(), Instant.now(), "Item1")
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(result).isEqualTo(items);
        }

        @Test
        @DisplayName("ASC 정렬에서 커서 이후 항목만 필터링")
        void withAscendingOrder_filtersItemsAfterCursor() {
            // given
            Instant cursor = Instant.parse("2024-01-01T12:00:00Z");
            UUID cursorId = UUID.fromString("00000000-0000-0000-0000-000000000001");

            UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
            UUID id3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

            List<TestItem> items = List.of(
                new TestItem(id1, Instant.parse("2024-01-01T12:00:00Z"), "Same time, same id"),
                new TestItem(id2, Instant.parse("2024-01-01T12:00:00Z"), "Same time, greater id"),
                new TestItem(id3, Instant.parse("2024-01-01T13:00:00Z"), "Greater time")
            );

            CursorRequest<TestSortField> request = createRequest(
                cursor.toString(), cursorId, 10, SortDirection.ASCENDING
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("Same time, greater id");
            assertThat(result.get(1).name()).isEqualTo("Greater time");
        }

        @Test
        @DisplayName("DESC 정렬에서 커서 이전 항목만 필터링")
        void withDescendingOrder_filtersItemsBeforeCursor() {
            // given
            Instant cursor = Instant.parse("2024-01-01T12:00:00Z");
            UUID cursorId = UUID.fromString("00000000-0000-0000-0000-000000000002");

            UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
            UUID id3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

            List<TestItem> items = List.of(
                new TestItem(id3, Instant.parse("2024-01-01T13:00:00Z"), "Greater time"),
                new TestItem(id2, Instant.parse("2024-01-01T12:00:00Z"), "Same time, same id"),
                new TestItem(id1, Instant.parse("2024-01-01T12:00:00Z"), "Same time, smaller id"),
                new TestItem(UUID.randomUUID(), Instant.parse("2024-01-01T11:00:00Z"), "Smaller time")
            );

            CursorRequest<TestSortField> request = createRequest(
                cursor.toString(), cursorId, 10, SortDirection.DESCENDING
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("Same time, smaller id");
            assertThat(result.get(1).name()).isEqualTo("Smaller time");
        }

        @Test
        @DisplayName("null 필드값을 가진 항목은 필터링됨")
        void withNullFieldValue_filtersOutItem() {
            // given
            Instant cursor = Instant.parse("2024-01-01T12:00:00Z");
            UUID cursorId = UUID.randomUUID();

            List<TestItem> items = List.of(
                new TestItem(UUID.randomUUID(), null, "Null createdAt"),
                new TestItem(UUID.randomUUID(), Instant.parse("2024-01-01T13:00:00Z"), "Valid")
            );

            CursorRequest<TestSortField> request = createRequest(
                cursor.toString(), cursorId, 10, SortDirection.ASCENDING
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("Valid");
        }

        @Test
        @DisplayName("ASC 정렬에서 필드값만 커서보다 큰 경우 포함")
        void withAscendingOrder_includesItemWithGreaterFieldOnly() {
            // given
            Instant cursor = Instant.parse("2024-01-01T12:00:00Z");
            UUID cursorId = UUID.fromString("00000000-0000-0000-0000-000000000005");

            UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");

            List<TestItem> items = List.of(
                new TestItem(id1, Instant.parse("2024-01-01T13:00:00Z"), "Greater time, smaller id")
            );

            CursorRequest<TestSortField> request = createRequest(
                cursor.toString(), cursorId, 10, SortDirection.ASCENDING
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then - cmp > 0 이므로 ID 비교 없이 포함됨
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("Greater time, smaller id");
        }

        @Test
        @DisplayName("cursor 역직렬화가 null을 반환하면 원본 리스트 반환")
        void withInvalidCursor_returnsOriginalList() {
            // given
            UUID cursorId = UUID.randomUUID();
            List<TestItem> items = List.of(
                new TestItem(UUID.randomUUID(), Instant.now(), "Item1")
            );

            CursorRequest<TestSortField> request = createRequest(
                "invalid-cursor", cursorId, 10, SortDirection.ASCENDING
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(result).isEqualTo(items);
        }

        @Test
        @DisplayName("null ID를 가진 항목은 필터링됨")
        void withNullId_filtersOutItem() {
            // given
            Instant cursor = Instant.parse("2024-01-01T12:00:00Z");
            UUID cursorId = UUID.randomUUID();

            List<TestItem> items = List.of(
                new TestItem(null, Instant.parse("2024-01-01T13:00:00Z"), "Null id"),
                new TestItem(UUID.randomUUID(), Instant.parse("2024-01-01T14:00:00Z"), "Valid")
            );

            CursorRequest<TestSortField> request = createRequest(
                cursor.toString(), cursorId, 10, SortDirection.ASCENDING
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("Valid");
        }

        @Test
        @DisplayName("ASC 정렬에서 같은 시간, 같은 ID는 제외됨")
        void withAscendingAndSameTimeAndSameId_excludesItem() {
            // given
            Instant cursor = Instant.parse("2024-01-01T12:00:00Z");
            UUID cursorId = UUID.fromString("00000000-0000-0000-0000-000000000002");

            UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001"); // smaller id
            UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002"); // same id
            UUID id3 = UUID.fromString("00000000-0000-0000-0000-000000000003"); // greater id

            List<TestItem> items = List.of(
                new TestItem(id1, cursor, "Same time, smaller id"),
                new TestItem(id2, cursor, "Same time, same id"),
                new TestItem(id3, cursor, "Same time, greater id")
            );

            CursorRequest<TestSortField> request = createRequest(
                cursor.toString(), cursorId, 10, SortDirection.ASCENDING
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then - cmp == 0인 경우: id1 < cursorId (제외), id2 == cursorId (제외), id3 > cursorId (포함)
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("Same time, greater id");
        }

        @Test
        @DisplayName("ASC 정렬에서 같은 시간, 더 작은 ID만 있으면 빈 결과 반환")
        void withAscendingAndSameTimeAndOnlySmallerIds_returnsEmpty() {
            // given
            Instant cursor = Instant.parse("2024-01-01T12:00:00Z");
            UUID cursorId = UUID.fromString("00000000-0000-0000-0000-000000000005");

            UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

            List<TestItem> items = List.of(
                new TestItem(id1, cursor, "Same time, smaller id 1"),
                new TestItem(id2, cursor, "Same time, smaller id 2")
            );

            CursorRequest<TestSortField> request = createRequest(
                cursor.toString(), cursorId, 10, SortDirection.ASCENDING
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then - cmp == 0이고 모든 ID가 cursorId보다 작으므로 모두 제외
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("ASC 정렬에서 필드값이 커서보다 작으면 제외됨")
        void withAscendingAndSmallerFieldValue_excludesItem() {
            // given
            Instant cursor = Instant.parse("2024-01-01T12:00:00Z");
            UUID cursorId = UUID.fromString("00000000-0000-0000-0000-000000000001");

            UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000005");

            List<TestItem> items = List.of(
                new TestItem(id1, Instant.parse("2024-01-01T11:00:00Z"), "Smaller time, greater id")
            );

            CursorRequest<TestSortField> request = createRequest(
                cursor.toString(), cursorId, 10, SortDirection.ASCENDING
            );

            // when
            List<TestItem> result = RedisCursorPaginationHelper.applyCursor(
                items,
                request,
                sortField,
                TestItem::createdAt,
                TestItem::id
            );

            // then - cmp < 0 이므로 ID와 상관없이 제외됨
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("buildResponse()")
    class BuildResponseTest {

        @Test
        @DisplayName("빈 결과일 때 empty 응답을 반환한다")
        void withEmptyRows_returnsEmptyResponse() {
            // given
            CursorRequest<TestSortField> request = createRequest(10);
            List<TestItem> items = List.of();

            // when
            CursorResponse<TestItem> response = RedisCursorPaginationHelper.buildResponse(
                items,
                request,
                sortField,
                0,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
            assertThat(response.nextIdAfter()).isNull();
            assertThat(response.totalCount()).isZero();
            assertThat(response.sortBy()).isEqualTo("CREATED_AT");
            assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);
        }

        @Test
        @DisplayName("결과가 limit보다 적으면 hasNext=false")
        void withFewerRowsThanLimit_hasNextIsFalse() {
            // given
            CursorRequest<TestSortField> request = createRequest(10);
            List<TestItem> items = List.of(
                new TestItem(UUID.randomUUID(), Instant.now(), "Item1"),
                new TestItem(UUID.randomUUID(), Instant.now(), "Item2")
            );

            // when
            CursorResponse<TestItem> response = RedisCursorPaginationHelper.buildResponse(
                items,
                request,
                sortField,
                2,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
            assertThat(response.nextIdAfter()).isNull();
            assertThat(response.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("결과가 limit+1개면 hasNext=true이고 마지막 행은 제외된다")
        void withMoreRowsThanLimit_hasNextIsTrue() {
            // given
            int limit = 2;
            CursorRequest<TestSortField> request = createRequest(limit);

            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UUID id3 = UUID.randomUUID();

            Instant time1 = Instant.parse("2024-01-01T10:00:00Z");
            Instant time2 = Instant.parse("2024-01-01T11:00:00Z");
            Instant time3 = Instant.parse("2024-01-01T12:00:00Z");

            List<TestItem> items = List.of(
                new TestItem(id1, time1, "Item1"),
                new TestItem(id2, time2, "Item2"),
                new TestItem(id3, time3, "Item3")
            );

            // when
            CursorResponse<TestItem> response = RedisCursorPaginationHelper.buildResponse(
                items,
                request,
                sortField,
                100,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data().get(0).name()).isEqualTo("Item1");
            assertThat(response.data().get(1).name()).isEqualTo("Item2");
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo(time2.toString());
            assertThat(response.nextIdAfter()).isEqualTo(id2);
            assertThat(response.totalCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("DESCENDING 방향이 응답에 반영된다")
        void withDescendingDirection_reflectsInResponse() {
            // given
            CursorRequest<TestSortField> request = createRequest(
                null, null, 10, SortDirection.DESCENDING
            );
            List<TestItem> items = List.of(
                new TestItem(UUID.randomUUID(), Instant.now(), "Test")
            );

            // when
            CursorResponse<TestItem> response = RedisCursorPaginationHelper.buildResponse(
                items,
                request,
                sortField,
                1,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(response.sortDirection()).isEqualTo(SortDirection.DESCENDING);
        }

        @Test
        @DisplayName("정확히 limit개의 결과면 hasNext=false")
        void withExactlyLimitRows_hasNextIsFalse() {
            // given
            int limit = 3;
            CursorRequest<TestSortField> request = createRequest(limit);

            List<TestItem> items = List.of(
                new TestItem(UUID.randomUUID(), Instant.now(), "Item1"),
                new TestItem(UUID.randomUUID(), Instant.now(), "Item2"),
                new TestItem(UUID.randomUUID(), Instant.now(), "Item3")
            );

            // when
            CursorResponse<TestItem> response = RedisCursorPaginationHelper.buildResponse(
                items,
                request,
                sortField,
                3,
                TestItem::createdAt,
                TestItem::id
            );

            // then
            assertThat(response.data()).hasSize(3);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("compareByFieldThenId()")
    class CompareByFieldThenIdTest {

        @Test
        @DisplayName("ASC 정렬에서 필드값이 다르면 필드값으로 비교")
        void withAscendingAndDifferentFields_comparesByField() {
            // given
            TestItem a = new TestItem(UUID.randomUUID(), Instant.parse("2024-01-01T10:00:00Z"),
                "A");
            TestItem b = new TestItem(UUID.randomUUID(), Instant.parse("2024-01-01T11:00:00Z"),
                "B");

            // when
            int result = RedisCursorPaginationHelper.compareByFieldThenId(
                a, b, TestItem::createdAt, TestItem::id, SortDirection.ASCENDING
            );

            // then
            assertThat(result).isLessThan(0);
        }

        @Test
        @DisplayName("ASC 정렬에서 필드값이 같으면 ID로 비교")
        void withAscendingAndSameFields_comparesById() {
            // given
            Instant sameTime = Instant.parse("2024-01-01T10:00:00Z");
            UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

            TestItem a = new TestItem(id1, sameTime, "A");
            TestItem b = new TestItem(id2, sameTime, "B");

            // when
            int result = RedisCursorPaginationHelper.compareByFieldThenId(
                a, b, TestItem::createdAt, TestItem::id, SortDirection.ASCENDING
            );

            // then
            assertThat(result).isLessThan(0);
        }

        @Test
        @DisplayName("DESC 정렬에서 결과가 반전됨")
        void withDescending_reversesResult() {
            // given
            TestItem a = new TestItem(UUID.randomUUID(), Instant.parse("2024-01-01T10:00:00Z"),
                "A");
            TestItem b = new TestItem(UUID.randomUUID(), Instant.parse("2024-01-01T11:00:00Z"),
                "B");

            // when
            int result = RedisCursorPaginationHelper.compareByFieldThenId(
                a, b, TestItem::createdAt, TestItem::id, SortDirection.DESCENDING
            );

            // then
            assertThat(result).isGreaterThan(0);
        }

        @Test
        @DisplayName("null 필드값은 뒤로 정렬됨")
        void withNullField_sortsToEnd() {
            // given
            TestItem a = new TestItem(UUID.randomUUID(), null, "A");
            TestItem b = new TestItem(UUID.randomUUID(), Instant.now(), "B");

            // when
            int result = RedisCursorPaginationHelper.compareByFieldThenId(
                a, b, TestItem::createdAt, TestItem::id, SortDirection.ASCENDING
            );

            // then
            assertThat(result).isGreaterThan(0);
        }

        @Test
        @DisplayName("둘 다 null 필드값이면 ID로 비교")
        void withBothNullFields_comparesById() {
            // given
            UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

            TestItem a = new TestItem(id1, null, "A");
            TestItem b = new TestItem(id2, null, "B");

            // when
            int result = RedisCursorPaginationHelper.compareByFieldThenId(
                a, b, TestItem::createdAt, TestItem::id, SortDirection.ASCENDING
            );

            // then
            assertThat(result).isLessThan(0);
        }

        @Test
        @DisplayName("b만 null 필드값이면 a가 앞으로 정렬됨")
        void withOnlyBNullField_aComesFirst() {
            // given
            TestItem a = new TestItem(UUID.randomUUID(), Instant.now(), "A");
            TestItem b = new TestItem(UUID.randomUUID(), null, "B");

            // when
            int result = RedisCursorPaginationHelper.compareByFieldThenId(
                a, b, TestItem::createdAt, TestItem::id, SortDirection.ASCENDING
            );

            // then
            assertThat(result).isLessThan(0);
        }

        @Test
        @DisplayName("a만 null ID이면 뒤로 정렬됨")
        void withOnlyANullId_aComesLast() {
            // given
            Instant sameTime = Instant.now();
            TestItem a = new TestItem(null, sameTime, "A");
            TestItem b = new TestItem(UUID.randomUUID(), sameTime, "B");

            // when
            int result = RedisCursorPaginationHelper.compareByFieldThenId(
                a, b, TestItem::createdAt, TestItem::id, SortDirection.ASCENDING
            );

            // then
            assertThat(result).isGreaterThan(0);
        }

        @Test
        @DisplayName("b만 null ID이면 a가 앞으로 정렬됨")
        void withOnlyBNullId_aComesFirst() {
            // given
            Instant sameTime = Instant.now();
            TestItem a = new TestItem(UUID.randomUUID(), sameTime, "A");
            TestItem b = new TestItem(null, sameTime, "B");

            // when
            int result = RedisCursorPaginationHelper.compareByFieldThenId(
                a, b, TestItem::createdAt, TestItem::id, SortDirection.ASCENDING
            );

            // then
            assertThat(result).isLessThan(0);
        }

        @Test
        @DisplayName("둘 다 null ID이면 0 반환")
        void withBothNullIds_returnsZero() {
            // given
            Instant sameTime = Instant.now();
            TestItem a = new TestItem(null, sameTime, "A");
            TestItem b = new TestItem(null, sameTime, "B");

            // when
            int result = RedisCursorPaginationHelper.compareByFieldThenId(
                a, b, TestItem::createdAt, TestItem::id, SortDirection.ASCENDING
            );

            // then
            assertThat(result).isZero();
        }
    }
}
