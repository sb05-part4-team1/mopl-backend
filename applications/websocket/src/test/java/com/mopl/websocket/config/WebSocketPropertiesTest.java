package com.mopl.websocket.config;

import com.mopl.websocket.config.WebSocketProperties.BroadcasterType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WebSocketProperties 단위 테스트")
class WebSocketPropertiesTest {

    @Nested
    @DisplayName("WebSocketProperties 레코드")
    class RecordTest {

        @Test
        @DisplayName("allowedOrigins와 broadcaster로 생성")
        void createsWithAllowedOriginsAndBroadcaster() {
            // given
            String allowedOrigins = "http://localhost:3000,http://localhost:8080";
            BroadcasterType broadcaster = BroadcasterType.local;

            // when
            WebSocketProperties properties = new WebSocketProperties(allowedOrigins, broadcaster);

            // then
            assertThat(properties).isNotNull();
            assertThat(properties.allowedOrigins()).isEqualTo(allowedOrigins);
            assertThat(properties.broadcaster()).isEqualTo(broadcaster);
        }

        @Test
        @DisplayName("redis broadcaster로 생성")
        void createsWithRedisBroadcaster() {
            // given
            String allowedOrigins = "http://localhost:3000";
            BroadcasterType broadcaster = BroadcasterType.redis;

            // when
            WebSocketProperties properties = new WebSocketProperties(allowedOrigins, broadcaster);

            // then
            assertThat(properties).isNotNull();
            assertThat(properties.allowedOrigins()).isEqualTo(allowedOrigins);
            assertThat(properties.broadcaster()).isEqualTo(BroadcasterType.redis);
        }

        @Test
        @DisplayName("equals - 동일한 값으로 생성된 레코드는 동일함")
        void equalsTest() {
            // given
            WebSocketProperties properties1 = new WebSocketProperties("http://localhost:3000", BroadcasterType.local);
            WebSocketProperties properties2 = new WebSocketProperties("http://localhost:3000", BroadcasterType.local);

            // then
            assertThat(properties1).isEqualTo(properties2);
        }

        @Test
        @DisplayName("hashCode - 동일한 값으로 생성된 레코드는 동일한 해시코드")
        void hashCodeTest() {
            // given
            WebSocketProperties properties1 = new WebSocketProperties("http://localhost:3000", BroadcasterType.local);
            WebSocketProperties properties2 = new WebSocketProperties("http://localhost:3000", BroadcasterType.local);

            // then
            assertThat(properties1.hashCode()).isEqualTo(properties2.hashCode());
        }

        @Test
        @DisplayName("toString - 레코드 문자열 표현")
        void toStringTest() {
            // given
            WebSocketProperties properties = new WebSocketProperties("http://localhost:3000", BroadcasterType.local);

            // when
            String result = properties.toString();

            // then
            assertThat(result).contains("WebSocketProperties");
            assertThat(result).contains("http://localhost:3000");
            assertThat(result).contains("local");
        }
    }

    @Nested
    @DisplayName("BroadcasterType Enum")
    class BroadcasterTypeTest {

        @Test
        @DisplayName("local 타입 존재")
        void localTypeExists() {
            // when
            BroadcasterType type = BroadcasterType.local;

            // then
            assertThat(type).isNotNull();
            assertThat(type.name()).isEqualTo("local");
        }

        @Test
        @DisplayName("redis 타입 존재")
        void redisTypeExists() {
            // when
            BroadcasterType type = BroadcasterType.redis;

            // then
            assertThat(type).isNotNull();
            assertThat(type.name()).isEqualTo("redis");
        }

        @Test
        @DisplayName("valueOf - 문자열로 타입 가져오기")
        void valueOfTest() {
            // when
            BroadcasterType local = BroadcasterType.valueOf("local");
            BroadcasterType redis = BroadcasterType.valueOf("redis");

            // then
            assertThat(local).isEqualTo(BroadcasterType.local);
            assertThat(redis).isEqualTo(BroadcasterType.redis);
        }

        @Test
        @DisplayName("values - 모든 타입 가져오기")
        void valuesTest() {
            // when
            BroadcasterType[] values = BroadcasterType.values();

            // then
            assertThat(values).hasSize(2);
            assertThat(values).containsExactlyInAnyOrder(BroadcasterType.local, BroadcasterType.redis);
        }
    }
}
