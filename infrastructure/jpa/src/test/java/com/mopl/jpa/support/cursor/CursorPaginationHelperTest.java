package com.mopl.jpa.support.cursor;

import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CursorPaginationHelper 단위 테스트")
class CursorPaginationHelperTest {

    enum TestSortField {
        NAME, EMAIL
    }

    static class TestSortFieldImpl implements SortField<String> {

        private final TestSortField sortField;
        private final ComparableExpression<String> expression;

        TestSortFieldImpl(TestSortField sortField) {
            this.sortField = sortField;
            this.expression = Expressions.comparableTemplate(String.class, sortField.name());
        }

        @Override
        public ComparableExpression<String> getExpression() {
            return expression;
        }

        @Override
        public String serializeCursor(Object value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public String deserializeCursor(String cursor) {
            return cursor;
        }

        @Override
        public String getFieldName() {
            return sortField.name();
        }
    }

    record TestRow(UUID id, String name) {
    }

    record TestDto(String displayName) {
    }

    @Nested
    @DisplayName("buildResponse()")
    class BuildResponseTest {

        private final SortField<?> sortField = new TestSortFieldImpl(TestSortField.NAME);

        private CursorRequest<TestSortField> createRequest(int limit) {
            return createRequest(limit, SortDirection.ASCENDING);
        }

        private CursorRequest<TestSortField> createRequest(
            int limit, SortDirection direction
        ) {
            return new CursorRequest<>() {

                @Override
                public String cursor() {
                    return null;
                }

                @Override
                public UUID idAfter() {
                    return null;
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
                    return TestSortField.NAME;
                }
            };
        }

        @Test
        @DisplayName("빈 결과일 때 empty 응답을 반환한다")
        void withEmptyRows_returnsEmptyResponse() {
            // given
            CursorRequest<TestSortField> request = createRequest(10);
            List<TestRow> rows = List.of();

            // when
            CursorResponse<TestDto> response = CursorPaginationHelper.buildResponse(
                rows,
                request,
                sortField,
                0,
                row -> new TestDto(row.name()),
                TestRow::name,
                TestRow::id
            );

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
            assertThat(response.nextIdAfter()).isNull();
            assertThat(response.totalCount()).isZero();
            assertThat(response.sortBy()).isEqualTo("NAME");
            assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);
        }

        @Test
        @DisplayName("결과가 limit보다 적으면 hasNext=false")
        void withFewerRowsThanLimit_hasNextIsFalse() {
            // given
            CursorRequest<TestSortField> request = createRequest(10);
            List<TestRow> rows = List.of(
                new TestRow(UUID.randomUUID(), "Alice"),
                new TestRow(UUID.randomUUID(), "Bob")
            );

            // when
            CursorResponse<TestDto> response = CursorPaginationHelper.buildResponse(
                rows,
                request,
                sortField,
                2,
                row -> new TestDto(row.name()),
                TestRow::name,
                TestRow::id
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

            List<TestRow> rows = List.of(
                new TestRow(id1, "Alice"),
                new TestRow(id2, "Bob"),
                new TestRow(id3, "Charlie") // limit+1번째 행 (제외됨)
            );

            // when
            CursorResponse<TestDto> response = CursorPaginationHelper.buildResponse(
                rows,
                request,
                sortField,
                100,
                row -> new TestDto(row.name()),
                TestRow::name,
                TestRow::id
            );

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data().getFirst().displayName()).isEqualTo("Alice");
            assertThat(response.data().get(1).displayName()).isEqualTo("Bob");
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo("Bob"); // 마지막 포함된 행의 커서
            assertThat(response.nextIdAfter()).isEqualTo(id2);
            assertThat(response.totalCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("DESCENDING 방향이 응답에 반영된다")
        void withDescendingDirection_reflectsInResponse() {
            // given
            CursorRequest<TestSortField> request = createRequest(
                10,
                SortDirection.DESCENDING
            );
            List<TestRow> rows = List.of(new TestRow(UUID.randomUUID(), "Test"));

            // when
            CursorResponse<TestDto> response = CursorPaginationHelper.buildResponse(
                rows,
                request,
                sortField,
                1,
                row -> new TestDto(row.name()),
                TestRow::name,
                TestRow::id
            );

            // then
            assertThat(response.sortDirection()).isEqualTo(SortDirection.DESCENDING);
        }

        @Test
        @DisplayName("mapper 함수가 각 행에 적용된다")
        void mapperFunctionAppliedToEachRow() {
            // given
            CursorRequest<TestSortField> request = createRequest(10);
            List<TestRow> rows = List.of(
                new TestRow(UUID.randomUUID(), "alice"),
                new TestRow(UUID.randomUUID(), "bob")
            );

            Function<TestRow, TestDto> mapper = row -> new TestDto(
                row.name().toUpperCase(Locale.ROOT)
            );

            // when
            CursorResponse<TestDto> response = CursorPaginationHelper.buildResponse(
                rows,
                request,
                sortField,
                2,
                mapper,
                TestRow::name,
                TestRow::id
            );

            // then
            assertThat(response.data())
                .extracting(TestDto::displayName)
                .containsExactly("ALICE", "BOB");
        }

        @Test
        @DisplayName("정확히 limit개의 결과면 hasNext=false")
        void withExactlyLimitRows_hasNextIsFalse() {
            // given
            int limit = 3;
            CursorRequest<TestSortField> request = createRequest(limit);

            List<TestRow> rows = List.of(
                new TestRow(UUID.randomUUID(), "Alice"),
                new TestRow(UUID.randomUUID(), "Bob"),
                new TestRow(UUID.randomUUID(), "Charlie")
            );

            // when
            CursorResponse<TestDto> response = CursorPaginationHelper.buildResponse(
                rows,
                request,
                sortField,
                3,
                row -> new TestDto(row.name()),
                TestRow::name,
                TestRow::id
            );

            // then
            assertThat(response.data()).hasSize(3);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("applyCursorPagination()")
    @ExtendWith(MockitoExtension.class)
    class ApplyCursorPaginationTest {

        @Mock
        JPAQuery<TestRow> query;

        private final ComparableExpression<UUID> idExpression = Expressions.comparableTemplate(UUID.class, "id");
        private final SortField<String> sortField = new TestSortFieldImpl(TestSortField.NAME);

        @Test
        @DisplayName("idAfter가 null이면 커서 조건 없이 정렬과 limit만 적용된다")
        void withNullIdAfter_appliesOnlyOrderAndLimit() {
            // given
            CursorRequest<TestSortField> request = new CursorRequest<>() {

                @Override
                public String cursor() {
                    return "someCursor";
                }

                @Override
                public UUID idAfter() {
                    return null;
                }

                @Override
                public Integer limit() {
                    return 10;
                }

                @Override
                public SortDirection sortDirection() {
                    return SortDirection.ASCENDING;
                }

                @Override
                public TestSortField sortBy() {
                    return TestSortField.NAME;
                }
            };

            when(query.where((BooleanExpression) any())).thenReturn(query);
            when(query.orderBy(any(OrderSpecifier[].class))).thenReturn(query);
            when(query.limit(anyLong())).thenReturn(query);

            // when
            CursorPaginationHelper.applyCursorPagination(request, sortField, query, idExpression);

            // then
            ArgumentCaptor<BooleanExpression> whereCaptor = ArgumentCaptor.forClass(BooleanExpression.class);
            verify(query).where(whereCaptor.capture());
            assertThat(whereCaptor.getValue()).isNull();
            verify(query).limit(11L);
        }

        @Test
        @DisplayName("cursor가 빈 문자열이면 커서 조건 없이 정렬과 limit만 적용된다")
        void withEmptyCursor_appliesOnlyOrderAndLimit() {
            // given
            CursorRequest<TestSortField> request = new CursorRequest<>() {

                @Override
                public String cursor() {
                    return "";
                }

                @Override
                public UUID idAfter() {
                    return UUID.randomUUID();
                }

                @Override
                public Integer limit() {
                    return 5;
                }

                @Override
                public SortDirection sortDirection() {
                    return SortDirection.DESCENDING;
                }

                @Override
                public TestSortField sortBy() {
                    return TestSortField.NAME;
                }
            };

            when(query.where((BooleanExpression) any())).thenReturn(query);
            when(query.orderBy(any(OrderSpecifier[].class))).thenReturn(query);
            when(query.limit(anyLong())).thenReturn(query);

            // when
            CursorPaginationHelper.applyCursorPagination(request, sortField, query, idExpression);

            // then
            ArgumentCaptor<BooleanExpression> whereCaptor = ArgumentCaptor.forClass(BooleanExpression.class);
            verify(query).where(whereCaptor.capture());
            assertThat(whereCaptor.getValue()).isNull();
            verify(query).limit(6L);
        }

        @Test
        @DisplayName("cursor가 null이면 커서 조건 없이 정렬과 limit만 적용된다")
        void withNullCursor_appliesOnlyOrderAndLimit() {
            // given
            CursorRequest<TestSortField> request = new CursorRequest<>() {

                @Override
                public String cursor() {
                    return null;
                }

                @Override
                public UUID idAfter() {
                    return UUID.randomUUID();
                }

                @Override
                public Integer limit() {
                    return 20;
                }

                @Override
                public SortDirection sortDirection() {
                    return SortDirection.ASCENDING;
                }

                @Override
                public TestSortField sortBy() {
                    return TestSortField.NAME;
                }
            };

            when(query.where((BooleanExpression) any())).thenReturn(query);
            when(query.orderBy(any(OrderSpecifier[].class))).thenReturn(query);
            when(query.limit(anyLong())).thenReturn(query);

            // when
            CursorPaginationHelper.applyCursorPagination(request, sortField, query, idExpression);

            // then
            ArgumentCaptor<BooleanExpression> whereCaptor = ArgumentCaptor.forClass(BooleanExpression.class);
            verify(query).where(whereCaptor.capture());
            assertThat(whereCaptor.getValue()).isNull();
            verify(query).limit(21L);
        }

        @Test
        @DisplayName("유효한 cursor와 idAfter가 있으면 ASCENDING 커서 조건이 적용된다")
        @SuppressWarnings("rawtypes")
        void withValidCursorAndIdAfterAscending_appliesCursorCondition() {
            // given
            UUID idAfter = UUID.randomUUID();
            CursorRequest<TestSortField> request = new CursorRequest<>() {

                @Override
                public String cursor() {
                    return "Alice";
                }

                @Override
                public UUID idAfter() {
                    return idAfter;
                }

                @Override
                public Integer limit() {
                    return 10;
                }

                @Override
                public SortDirection sortDirection() {
                    return SortDirection.ASCENDING;
                }

                @Override
                public TestSortField sortBy() {
                    return TestSortField.NAME;
                }
            };

            when(query.where((BooleanExpression) any())).thenReturn(query);
            when(query.orderBy(any(OrderSpecifier[].class))).thenReturn(query);
            when(query.limit(anyLong())).thenReturn(query);

            // when
            CursorPaginationHelper.applyCursorPagination(request, sortField, query, idExpression);

            // then
            ArgumentCaptor<BooleanExpression> whereCaptor = ArgumentCaptor.forClass(BooleanExpression.class);
            verify(query).where(whereCaptor.capture());
            assertThat(whereCaptor.getValue()).isNotNull();

            ArgumentCaptor<OrderSpecifier[]> orderCaptor = ArgumentCaptor.forClass(OrderSpecifier[].class);
            verify(query).orderBy(orderCaptor.capture());
            OrderSpecifier<?>[] orderSpecifiers = orderCaptor.getValue();
            assertThat(orderSpecifiers).hasSize(2);
            assertThat(orderSpecifiers[0].getOrder()).isEqualTo(Order.ASC);
            assertThat(orderSpecifiers[1].getOrder()).isEqualTo(Order.ASC);
        }

        @Test
        @DisplayName("유효한 cursor와 idAfter가 있으면 DESCENDING 커서 조건이 적용된다")
        @SuppressWarnings("rawtypes")
        void withValidCursorAndIdAfterDescending_appliesCursorCondition() {
            // given
            UUID idAfter = UUID.randomUUID();
            CursorRequest<TestSortField> request = new CursorRequest<>() {

                @Override
                public String cursor() {
                    return "Zoe";
                }

                @Override
                public UUID idAfter() {
                    return idAfter;
                }

                @Override
                public Integer limit() {
                    return 15;
                }

                @Override
                public SortDirection sortDirection() {
                    return SortDirection.DESCENDING;
                }

                @Override
                public TestSortField sortBy() {
                    return TestSortField.NAME;
                }
            };

            when(query.where((BooleanExpression) any())).thenReturn(query);
            when(query.orderBy(any(OrderSpecifier[].class))).thenReturn(query);
            when(query.limit(anyLong())).thenReturn(query);

            // when
            CursorPaginationHelper.applyCursorPagination(request, sortField, query, idExpression);

            // then
            ArgumentCaptor<BooleanExpression> whereCaptor = ArgumentCaptor.forClass(BooleanExpression.class);
            verify(query).where(whereCaptor.capture());
            assertThat(whereCaptor.getValue()).isNotNull();

            ArgumentCaptor<OrderSpecifier[]> orderCaptor = ArgumentCaptor.forClass(OrderSpecifier[].class);
            verify(query).orderBy(orderCaptor.capture());
            OrderSpecifier<?>[] orderSpecifiers = orderCaptor.getValue();
            assertThat(orderSpecifiers).hasSize(2);
            assertThat(orderSpecifiers[0].getOrder()).isEqualTo(Order.DESC);
            assertThat(orderSpecifiers[1].getOrder()).isEqualTo(Order.DESC);
        }
    }
}
