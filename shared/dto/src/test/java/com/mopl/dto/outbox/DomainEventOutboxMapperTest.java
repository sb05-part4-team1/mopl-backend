package com.mopl.dto.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.event.DomainEvent;
import com.mopl.domain.exception.outbox.EventSerializationException;
import com.mopl.domain.model.outbox.OutboxModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("DomainEventOutboxMapper 단위 테스트")
class DomainEventOutboxMapperTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DomainEventOutboxMapper domainEventOutboxMapper;

    @Nested
    @DisplayName("toOutboxModel()")
    class ToOutboxModelTest {

        @Test
        @DisplayName("DomainEvent를 OutboxModel로 변환한다")
        void withValidEvent_returnsOutboxModel() throws JsonProcessingException {
            // given
            DomainEvent event = new TestDomainEvent();
            String expectedPayload = "{\"test\":\"data\"}";
            given(objectMapper.writeValueAsString(event)).willReturn(expectedPayload);

            // when
            OutboxModel result = domainEventOutboxMapper.toOutboxModel(event);

            // then
            assertThat(result.getAggregateType()).isEqualTo("TestAggregate");
            assertThat(result.getAggregateId()).isEqualTo("test-id-123");
            assertThat(result.getEventType()).isEqualTo("TestDomainEvent");
            assertThat(result.getTopic()).isEqualTo("test.topic");
            assertThat(result.getPayload()).isEqualTo(expectedPayload);
            assertThat(result.getStatus()).isEqualTo(OutboxModel.OutboxStatus.PENDING);
        }

        @Test
        @DisplayName("직렬화 실패 시 EventSerializationException을 던진다")
        void withSerializationFailure_throwsEventSerializationException() throws JsonProcessingException {
            // given
            DomainEvent event = new TestDomainEvent();
            given(objectMapper.writeValueAsString(event))
                .willThrow(new JsonProcessingException("Serialization failed") {
                });

            // when & then
            assertThatThrownBy(() -> domainEventOutboxMapper.toOutboxModel(event))
                .isInstanceOf(EventSerializationException.class);
        }
    }

    private static class TestDomainEvent implements DomainEvent {

        @Override
        public String getAggregateType() {
            return "TestAggregate";
        }

        @Override
        public String getAggregateId() {
            return "test-id-123";
        }

        @Override
        public String getTopic() {
            return "test.topic";
        }
    }
}
