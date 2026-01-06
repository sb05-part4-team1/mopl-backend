package com.mopl.domain.support.cursor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CursorResponse 단위 테스트")
class CursorResponseTest {

    @Nested
    @DisplayName("of()")
    class OfTest {

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void withAllFields_createsResponseCorrectly() {
            // given
            List<String> data = List.of("item1", "item2");
            String nextCursor = "cursor123";
            UUID nextIdAfter = UUID.randomUUID();
            long totalCount = 100;

            // when
            CursorResponse<String> response = CursorResponse.of(
                data,
                nextCursor,
                nextIdAfter,
                true,
                totalCount,
                "name",
                SortDirection.ASCENDING
            );

            // then
            assertThat(response.data()).isEqualTo(data);
            assertThat(response.nextCursor()).isEqualTo(nextCursor);
            assertThat(response.nextIdAfter()).isEqualTo(nextIdAfter);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.totalCount()).isEqualTo(totalCount);
            assertThat(response.sortBy()).isEqualTo("name");
            assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);
        }

        @Test
        @DisplayName("hasNext가 false이면 커서 정보가 null이다")
        void withNoNextPage_hasNullCursorInfo() {
            // given
            List<String> data = List.of("item1");

            // when
            CursorResponse<String> response = CursorResponse.of(
                data,
                null,
                null,
                false,
                1,
                "email",
                SortDirection.DESCENDING
            );

            // then
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
            assertThat(response.nextIdAfter()).isNull();
        }
    }

    @Nested
    @DisplayName("empty()")
    class EmptyTest {

        @Test
        @DisplayName("빈 응답을 올바르게 생성한다")
        void createsEmptyResponse() {
            // when
            CursorResponse<String> response = CursorResponse.empty("createdAt", SortDirection.DESCENDING);

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.nextCursor()).isNull();
            assertThat(response.nextIdAfter()).isNull();
            assertThat(response.hasNext()).isFalse();
            assertThat(response.totalCount()).isZero();
            assertThat(response.sortBy()).isEqualTo("createdAt");
            assertThat(response.sortDirection()).isEqualTo(SortDirection.DESCENDING);
        }
    }

    @Nested
    @DisplayName("map()")
    class MapTest {

        @Test
        @DisplayName("데이터를 변환하면서 메타데이터는 유지한다")
        void transformsDataWhilePreservingMetadata() {
            // given
            List<Integer> originalData = List.of(1, 2, 3);
            String nextCursor = "cursor456";
            UUID nextIdAfter = UUID.randomUUID();

            CursorResponse<Integer> original = CursorResponse.of(
                originalData,
                nextCursor,
                nextIdAfter,
                true,
                50,
                "id",
                SortDirection.ASCENDING
            );

            // when
            CursorResponse<String> mapped = original.map(num -> "item" + num);

            // then
            assertThat(mapped.data()).containsExactly("item1", "item2", "item3");
            assertThat(mapped.nextCursor()).isEqualTo(nextCursor);
            assertThat(mapped.nextIdAfter()).isEqualTo(nextIdAfter);
            assertThat(mapped.hasNext()).isTrue();
            assertThat(mapped.totalCount()).isEqualTo(50);
            assertThat(mapped.sortBy()).isEqualTo("id");
            assertThat(mapped.sortDirection()).isEqualTo(SortDirection.ASCENDING);
        }

        @Test
        @DisplayName("빈 데이터를 변환해도 빈 리스트를 반환한다")
        void withEmptyData_returnsEmptyList() {
            // given
            CursorResponse<Integer> empty = CursorResponse.empty("name", SortDirection.ASCENDING);

            // when
            CursorResponse<String> mapped = empty.map(num -> "item" + num);

            // then
            assertThat(mapped.data()).isEmpty();
        }
    }
}
