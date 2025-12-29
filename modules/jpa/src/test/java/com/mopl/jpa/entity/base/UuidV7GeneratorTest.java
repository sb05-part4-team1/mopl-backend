package com.mopl.jpa.entity.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UuidV7Generator 단위 테스트")
class UuidV7GeneratorTest {

    private final UuidV7Generator generator = new UuidV7Generator();

    @Nested
    @DisplayName("generate()")
    class GenerateTest {

        @Test
        @DisplayName("UUID v7을 반환함")
        void withGenerate_returnsUuidV7() {
            // when
            UUID uuid = (UUID) generator.generate(null, null);

            // then
            assertThat(uuid).isNotNull();
            assertThat(uuid.version()).isEqualTo(7);
        }

        @Test
        @DisplayName("여러 번 호출 시 고유한 UUID를 반환함")
        void withMultipleCalls_returnsUniqueUuids() {
            // given
            int count = 1000;
            Set<UUID> uuids = new HashSet<>();

            // when
            for (int i = 0; i < count; i++) {
                uuids.add((UUID) generator.generate(null, null));
            }

            // then
            assertThat(uuids).hasSize(count);
        }

        @Test
        @DisplayName("연속 호출 시 시간순 정렬 가능한 UUID를 반환함")
        void withConsecutiveCalls_returnsTimeOrderedUuids() {
            // given
            List<UUID> uuids = new ArrayList<>();

            // when
            for (int i = 0; i < 100; i++) {
                uuids.add((UUID) generator.generate(null, null));
            }

            // then
            List<UUID> sorted = new ArrayList<>(uuids);
            Collections.sort(sorted);
            assertThat(uuids).isEqualTo(sorted);
        }
    }
}
