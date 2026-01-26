package com.mopl.jpa.entity.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConversationEntityMapper 단위 테스트")
class ConversationEntityMapperTest {

    private final ConversationEntityMapper mapper = new ConversationEntityMapper();

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("ConversationEntity를 ConversationModel로 변환")
        void withConversationEntity_returnsConversationModel() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            ConversationEntity entity = ConversationEntity.builder()
                .id(id)
                .createdAt(now)
                .updatedAt(now)
                .build();

            // when
            ConversationModel result = mapper.toModel(entity);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            ConversationModel result = mapper.toModel(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("ConversationModel을 ConversationEntity로 변환")
        void withConversationModel_returnsConversationEntity() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            ConversationModel model = ConversationModel.builder()
                .id(id)
                .createdAt(now)
                .updatedAt(now)
                .build();

            // when
            ConversationEntity result = mapper.toEntity(model);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            ConversationEntity result = mapper.toEntity(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("양방향 변환 시 데이터 유지")
        void roundTrip_preservesData() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            ConversationModel originalModel = ConversationModel.builder()
                .id(id)
                .createdAt(now)
                .updatedAt(now)
                .build();

            // when
            ConversationEntity entity = mapper.toEntity(originalModel);
            ConversationModel resultModel = mapper.toModel(entity);

            // then
            assertThat(resultModel.getId()).isEqualTo(originalModel.getId());
            assertThat(resultModel.getCreatedAt()).isEqualTo(originalModel.getCreatedAt());
            assertThat(resultModel.getUpdatedAt()).isEqualTo(originalModel.getUpdatedAt());
        }
    }
}
