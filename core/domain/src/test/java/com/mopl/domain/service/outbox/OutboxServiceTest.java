package com.mopl.domain.service.outbox;

import com.mopl.domain.fixture.OutboxModelFixture;
import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.domain.repository.outbox.OutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxService 단위 테스트")
class OutboxServiceTest {

    @Mock
    private OutboxRepository outboxRepository;

    @InjectMocks
    private OutboxService outboxService;

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("유효한 OutboxModel 저장 시 저장된 OutboxModel 반환")
        void withValidOutboxModel_returnsSavedOutboxModel() {
            // given
            OutboxModel outboxModel = OutboxModelFixture.create();
            given(outboxRepository.save(outboxModel)).willReturn(outboxModel);

            // when
            OutboxModel result = outboxService.save(outboxModel);

            // then
            assertThat(result).isEqualTo(outboxModel);
            then(outboxRepository).should().save(outboxModel);
        }

        @Test
        @DisplayName("PENDING 상태의 OutboxModel 저장 성공")
        void withPendingStatus_saveSuccess() {
            // given
            OutboxModel outboxModel = OutboxModelFixture.createWithStatus(OutboxModel.OutboxStatus.PENDING);
            given(outboxRepository.save(outboxModel)).willReturn(outboxModel);

            // when
            OutboxModel result = outboxService.save(outboxModel);

            // then
            assertThat(result.isPending()).isTrue();
            then(outboxRepository).should().save(outboxModel);
        }
    }
}
