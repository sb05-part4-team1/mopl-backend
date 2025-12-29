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
            given(followRepository.save(any(FollowModel.class)))
                .willReturn(savedFollow);

            // when
            FollowModel result = followService.create(followModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getFollowerId()).isEqualTo(followerId);
            assertThat(result.getFolloweeId()).isEqualTo(followeeId);
            then(followRepository).should().findByFollowerIdAndFolloweeId(followerId, followeeId);
            then(followRepository).should().save(any(FollowModel.class));
        }

        @Test
        @DisplayName("자기 자신을 팔로우하면 예외 발생")
        void givenSelfFollowRequest_whenCreate_thenThrowsIllegalArgumentException() {
            // given
            UUID sameUserId = UUID.randomUUID();
            FollowModel selfFollowModel = FollowModel.create(sameUserId, sameUserId);

            // when & then
            assertThatThrownBy(() -> followService.create(selfFollowModel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자기 자신을 팔로우할 수 없습니다.");

            then(followRepository).should(never()).findByFollowerIdAndFolloweeId(any(), any());
            then(followRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 팔로우 중이면 예외 발생")
        void givenAlreadyExistingFollow_whenCreate_thenThrowsIllegalStateException() {
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

            // when & then
            assertThatThrownBy(() -> followService.create(followModel))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 팔로우 중인 사용자입니다.");

            then(followRepository).should().findByFollowerIdAndFolloweeId(followerId, followeeId);
            then(followRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("삭제된 팔로우 관계를 복구하여 저장")
        void givenDeletedFollowHistory_whenCreate_thenRestoresAndSaves() {
            // given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            FollowModel followModel = FollowModel.create(followeeId, followerId);
            FollowModel deletedFollow = FollowModel.builder()
                .id(UUID.randomUUID())
                .followeeId(followeeId)
                .followerId(followerId)
                .build();
            deletedFollow.delete();

            FollowModel restoredFollow = FollowModel.builder()
                .id(deletedFollow.getId())
                .followeeId(followeeId)
                .followerId(followerId)
                .deletedAt(null)
                .build();

            given(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
                .willReturn(Optional.of(deletedFollow));
            given(followRepository.save(deletedFollow))
                .willReturn(restoredFollow);

            // when
            FollowModel result = followService.create(followModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(deletedFollow.getId());
            assertThat(result.getFollowerId()).isEqualTo(followerId);
            assertThat(result.getFolloweeId()).isEqualTo(followeeId);
            then(followRepository).should().findByFollowerIdAndFolloweeId(followerId, followeeId);
            then(followRepository).should().save(deletedFollow);
        }
    }
}
