package com.mopl.domain.service.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mopl.domain.exception.user.SelfFollowException;
import com.mopl.domain.model.user.FollowModel;
import com.mopl.domain.repository.user.FollowRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService 단위 테스트")
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FollowService followService;

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("새로운 팔로우 관계 생성")
        void givenNonExistingFollow_whenCreate_thenSuccess() {
            // given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            FollowModel followModel = FollowModel.create(followeeId, followerId);
            FollowModel savedFollow = FollowModel.builder()
                .id(UUID.randomUUID())
                .followeeId(followeeId)
                .followerId(followerId)
                .build();

            given(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
                .willReturn(Optional.empty());
            given(followRepository.save(followModel))
                .willReturn(savedFollow);

            // when
            FollowModel result = followService.create(followModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getFollowerId()).isEqualTo(followerId);
            assertThat(result.getFolloweeId()).isEqualTo(followeeId);

            then(followRepository).should().findByFollowerIdAndFolloweeId(followerId, followeeId);
            then(followRepository).should().save(followModel);
        }

        @Test
        @DisplayName("이미 팔로우 중이면 예외 없이 기존 관계 반환")
        void givenAlreadyExistingFollow_whenCreate_thenReturnExistingFollow() {
            // given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            FollowModel followModel = FollowModel.create(followeeId, followerId);
            FollowModel existingFollow = FollowModel.builder()
                .id(UUID.randomUUID())
                .followeeId(followeeId)
                .followerId(followerId)
                .deletedAt(null)
                .build();

            given(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
                .willReturn(Optional.of(existingFollow));

            // when
            FollowModel result = followService.create(followModel);

            // then
            assertThat(result).isEqualTo(existingFollow);
            assertThat(result.getId()).isEqualTo(existingFollow.getId());

            then(followRepository).should().findByFollowerIdAndFolloweeId(followerId, followeeId);
            then(followRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("자기 자신을 팔로우 하면 SelfFollowException 발생")
        void givenSelfFollowRequest_whenCreate_thenThrowsSelfFollowException() {
            // given
            UUID sameUserId = UUID.randomUUID();
            FollowModel selfFollowModel = FollowModel.create(sameUserId, sameUserId);

            // when & then
            assertThatThrownBy(() -> followService.create(selfFollowModel))
                .isInstanceOf(SelfFollowException.class)
                .hasMessage(SelfFollowException.MESSAGE);

            then(followRepository).should(never()).findByFollowerIdAndFolloweeId(any(), any());
            then(followRepository).should(never()).save(any());
        }
    }
}
