package com.mopl.domain.service.follow;

import com.mopl.domain.exception.follow.FollowAlreadyExistsException;
import com.mopl.domain.exception.follow.FollowErrorCode;
import com.mopl.domain.exception.follow.FollowNotFoundException;
import com.mopl.domain.exception.follow.SelfFollowException;
import com.mopl.domain.model.follow.FollowModel;
import com.mopl.domain.repository.follow.FollowRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

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

            given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId))
                .willReturn(false);
            given(followRepository.save(followModel))
                .willReturn(savedFollow);

            // when
            FollowModel result = followService.create(followModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getFollowerId()).isEqualTo(followerId);
            assertThat(result.getFolloweeId()).isEqualTo(followeeId);

            then(followRepository).should().existsByFollowerIdAndFolloweeId(followerId, followeeId);
            then(followRepository).should().save(followModel);
        }

        @Test
        @DisplayName("이미 팔로우 중이면 FollowAlreadyExistsException 발생")
        void givenAlreadyExistingFollow_whenCreate_thenThrowsFollowAlreadyExistsException() {
            // given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            FollowModel followModel = FollowModel.create(followeeId, followerId);

            given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId))
                .willReturn(true);

            // when & then
            assertThatThrownBy(() -> followService.create(followModel))
                .isInstanceOf(FollowAlreadyExistsException.class)
                .hasMessage(FollowErrorCode.FOLLOW_ALREADY_EXISTS.getMessage());

            then(followRepository).should().existsByFollowerIdAndFolloweeId(followerId, followeeId);
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
                .hasMessage(FollowErrorCode.SELF_FOLLOW_NOT_ALLOWED.getMessage());

            then(followRepository).should(never()).existsByFollowerIdAndFolloweeId(any(), any());
            then(followRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTest {

        @Test
        @DisplayName("존재하는 팔로우 ID로 조회하면 FollowModel 반환")
        void withExistingId_returnsFollowModel() {
            // given
            UUID followId = UUID.randomUUID();
            FollowModel followModel = FollowModel.builder()
                .id(followId)
                .followerId(UUID.randomUUID())
                .followeeId(UUID.randomUUID())
                .build();

            given(followRepository.findById(followId)).willReturn(Optional.of(followModel));

            // when
            FollowModel result = followService.getById(followId);

            // then
            assertThat(result).isEqualTo(followModel);
            then(followRepository).should().findById(followId);
        }

        @Test
        @DisplayName("존재하지 않는 팔로우 ID로 조회하면 FollowNotFoundException 발생")
        void withNonExistingId_throwsFollowNotFoundException() {
            // given
            UUID followId = UUID.randomUUID();

            given(followRepository.findById(followId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.getById(followId))
                .isInstanceOf(FollowNotFoundException.class)
                .hasMessage(FollowErrorCode.FOLLOW_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("getByFollowerIdAndFolloweeId()")
    class GetByFollowerIdAndFolloweeIdTest {

        @Test
        @DisplayName("팔로우 관계가 존재하면 Optional에 FollowModel 반환")
        void withExistingFollow_returnsOptionalOfFollowModel() {
            // given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            FollowModel followModel = FollowModel.builder()
                .id(UUID.randomUUID())
                .followerId(followerId)
                .followeeId(followeeId)
                .build();

            given(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
                .willReturn(Optional.of(followModel));

            // when
            Optional<FollowModel> result = followService.getByFollowerIdAndFolloweeId(followerId, followeeId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getFollowerId()).isEqualTo(followerId);
            assertThat(result.get().getFolloweeId()).isEqualTo(followeeId);
            then(followRepository).should().findByFollowerIdAndFolloweeId(followerId, followeeId);
        }

        @Test
        @DisplayName("팔로우 관계가 없으면 빈 Optional 반환")
        void withNoFollow_returnsEmptyOptional() {
            // given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();

            given(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
                .willReturn(Optional.empty());

            // when
            Optional<FollowModel> result = followService.getByFollowerIdAndFolloweeId(followerId, followeeId);

            // then
            assertThat(result).isEmpty();
            then(followRepository).should().findByFollowerIdAndFolloweeId(followerId, followeeId);
        }
    }

    @Nested
    @DisplayName("getFollowerIds()")
    class GetFollowerIdsTest {

        @Test
        @DisplayName("팔로워 ID 목록 반환")
        void returnsFollowerIds() {
            // given
            UUID followeeId = UUID.randomUUID();
            UUID followerId1 = UUID.randomUUID();
            UUID followerId2 = UUID.randomUUID();
            List<UUID> followerIds = List.of(followerId1, followerId2);

            given(followRepository.findFollowerIdsByFolloweeId(followeeId)).willReturn(followerIds);

            // when
            List<UUID> result = followService.getFollowerIds(followeeId);

            // then
            assertThat(result).containsExactly(followerId1, followerId2);
            then(followRepository).should().findFollowerIdsByFolloweeId(followeeId);
        }

        @Test
        @DisplayName("팔로워가 없으면 빈 목록 반환")
        void withNoFollowers_returnsEmptyList() {
            // given
            UUID followeeId = UUID.randomUUID();

            given(followRepository.findFollowerIdsByFolloweeId(followeeId)).willReturn(List.of());

            // when
            List<UUID> result = followService.getFollowerIds(followeeId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getFollowerCount()")
    class GetFollowerCountTest {

        @Test
        @DisplayName("팔로워 수 반환")
        void returnsFollowerCount() {
            // given
            UUID followeeId = UUID.randomUUID();

            given(followRepository.countByFolloweeId(followeeId)).willReturn(42L);

            // when
            long result = followService.getFollowerCount(followeeId);

            // then
            assertThat(result).isEqualTo(42L);
            then(followRepository).should().countByFolloweeId(followeeId);
        }

        @Test
        @DisplayName("팔로워가 없으면 0 반환")
        void withNoFollowers_returnsZero() {
            // given
            UUID followeeId = UUID.randomUUID();

            given(followRepository.countByFolloweeId(followeeId)).willReturn(0L);

            // when
            long result = followService.getFollowerCount(followeeId);

            // then
            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("isFollow()")
    class IsFollowTest {

        @Test
        @DisplayName("팔로우 관계가 존재하면 true 반환")
        void withExistingFollow_returnsTrue() {
            // given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();

            given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId))
                .willReturn(true);

            // when
            boolean result = followService.isFollow(followerId, followeeId);

            // then
            assertThat(result).isTrue();
            then(followRepository).should().existsByFollowerIdAndFolloweeId(followerId, followeeId);
        }

        @Test
        @DisplayName("팔로우 관계가 없으면 false 반환")
        void withNoFollow_returnsFalse() {
            // given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();

            given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId))
                .willReturn(false);

            // when
            boolean result = followService.isFollow(followerId, followeeId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("팔로우 삭제")
        void deletesFollow() {
            // given
            FollowModel followModel = FollowModel.builder()
                .id(UUID.randomUUID())
                .followerId(UUID.randomUUID())
                .followeeId(UUID.randomUUID())
                .build();

            // when
            followService.delete(followModel);

            // then
            then(followRepository).should().delete(followModel);
        }
    }
}
