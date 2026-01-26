package com.mopl.api.application.follow;

import com.mopl.domain.event.user.UserFollowedEvent;
import com.mopl.domain.event.user.UserUnfollowedEvent;
import com.mopl.domain.exception.follow.FollowNotAllowedException;
import com.mopl.domain.fixture.FollowModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.follow.FollowModel;
import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.outbox.OutboxService;
import com.mopl.domain.service.user.UserService;
import com.mopl.dto.outbox.DomainEventOutboxMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowFacade 단위 테스트")
class FollowFacadeTest {

    @Mock
    private FollowService followService;

    @Mock
    private UserService userService;

    @Mock
    private OutboxService outboxService;

    @Mock
    private DomainEventOutboxMapper domainEventOutboxMapper;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private FollowFacade followFacade;

    @Nested
    @DisplayName("follow()")
    class FollowTest {

        @Test
        @DisplayName("유효한 요청 시 팔로우 성공 및 이벤트 발행")
        void withValidRequest_followSuccess() {
            // given
            UserModel follower = UserModelFixture.create();
            UserModel followee = UserModelFixture.create();

            FollowModel savedFollow = FollowModelFixture.builder()
                .set("followerId", follower.getId())
                .set("followeeId", followee.getId())
                .sample();

            OutboxModel outboxModel = mock(OutboxModel.class);

            given(userService.getById(follower.getId())).willReturn(follower);
            given(userService.getById(followee.getId())).willReturn(followee);
            given(followService.create(any(FollowModel.class))).willReturn(savedFollow);
            given(domainEventOutboxMapper.toOutboxModel(any(UserFollowedEvent.class)))
                .willReturn(outboxModel);
            willAnswer(invocation -> invocation.<TransactionCallback<?>>getArgument(0)
                .doInTransaction(mock(TransactionStatus.class)))
                .given(transactionTemplate).execute(any());

            // when
            FollowModel result = followFacade.follow(follower.getId(), followee.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getFollowerId()).isEqualTo(follower.getId());
            assertThat(result.getFolloweeId()).isEqualTo(followee.getId());

            then(userService).should().getById(follower.getId());
            then(userService).should().getById(followee.getId());
            then(followService).should().create(any(FollowModel.class));
            then(outboxService).should().save(outboxModel);
        }

        @Test
        @DisplayName("팔로우 시 UserFollowedEvent가 생성된다")
        void withFollow_createsUserFollowedEvent() {
            // given
            UserModel follower = UserModelFixture.builder()
                .set("name", "Follower")
                .sample();
            UserModel followee = UserModelFixture.create();

            FollowModel savedFollow = FollowModelFixture.builder()
                .set("followerId", follower.getId())
                .set("followeeId", followee.getId())
                .sample();

            given(userService.getById(follower.getId())).willReturn(follower);
            given(userService.getById(followee.getId())).willReturn(followee);
            given(followService.create(any(FollowModel.class))).willReturn(savedFollow);
            willAnswer(invocation -> invocation.<TransactionCallback<?>>getArgument(0)
                .doInTransaction(mock(TransactionStatus.class)))
                .given(transactionTemplate).execute(any());

            // when
            followFacade.follow(follower.getId(), followee.getId());

            // then
            then(domainEventOutboxMapper).should().toOutboxModel(any(UserFollowedEvent.class));
        }
    }

    @Nested
    @DisplayName("unFollow()")
    class UnFollowTest {

        @Test
        @DisplayName("본인의 팔로우 삭제 성공 및 이벤트 발행")
        void withOwnFollow_unFollowSuccess() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel user = UserModelFixture.builder()
                .set("id", userId)
                .sample();

            FollowModel follow = FollowModelFixture.builder()
                .set("followerId", userId)
                .set("followeeId", UUID.randomUUID())
                .sample();

            OutboxModel outboxModel = mock(OutboxModel.class);

            given(userService.getById(userId)).willReturn(user);
            given(followService.getById(follow.getId())).willReturn(follow);
            given(domainEventOutboxMapper.toOutboxModel(any(UserUnfollowedEvent.class)))
                .willReturn(outboxModel);
            willAnswer(invocation -> {
                invocation.<Consumer<Object>>getArgument(0).accept(null);
                return null;
            }).given(transactionTemplate).executeWithoutResult(any());

            // when
            followFacade.unFollow(userId, follow.getId());

            // then
            then(userService).should().getById(userId);
            then(followService).should().getById(follow.getId());
            then(followService).should().delete(follow);
            then(outboxService).should().save(outboxModel);
        }

        @Test
        @DisplayName("다른 사람의 팔로우 삭제 시 FollowNotAllowedException 발생")
        void withOthersFollow_throwsFollowNotAllowedException() {
            // given
            UUID userId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            UserModel user = UserModelFixture.builder()
                .set("id", userId)
                .sample();

            FollowModel follow = FollowModelFixture.builder()
                .set("followerId", otherUserId)
                .sample();

            given(userService.getById(userId)).willReturn(user);
            given(followService.getById(follow.getId())).willReturn(follow);

            // when & then
            assertThatThrownBy(() -> followFacade.unFollow(userId, follow.getId()))
                .isInstanceOf(FollowNotAllowedException.class);

            then(followService).should(never()).delete(any());
            then(outboxService).should(never()).save(any());
        }

        @Test
        @DisplayName("언팔로우 시 UserUnfollowedEvent가 생성된다")
        void withUnFollow_createsUserUnfollowedEvent() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel user = UserModelFixture.builder()
                .set("id", userId)
                .sample();

            FollowModel follow = FollowModelFixture.builder()
                .set("followerId", userId)
                .sample();

            given(userService.getById(userId)).willReturn(user);
            given(followService.getById(follow.getId())).willReturn(follow);
            willAnswer(invocation -> {
                invocation.<Consumer<Object>>getArgument(0).accept(null);
                return null;
            }).given(transactionTemplate).executeWithoutResult(any());

            // when
            followFacade.unFollow(userId, follow.getId());

            // then
            then(domainEventOutboxMapper).should().toOutboxModel(any(UserUnfollowedEvent.class));
        }
    }

    @Nested
    @DisplayName("getFollowerCount()")
    class GetFollowerCountTest {

        @Test
        @DisplayName("존재하는 사용자의 팔로워 수 조회 성공")
        void withExistingUser_returnsFollowerCount() {
            // given
            UUID followeeId = UUID.randomUUID();
            UserModel user = UserModelFixture.builder()
                .set("id", followeeId)
                .sample();

            given(userService.getById(followeeId)).willReturn(user);
            given(followService.getFollowerCount(followeeId)).willReturn(42L);

            // when
            long result = followFacade.getFollowerCount(followeeId);

            // then
            assertThat(result).isEqualTo(42L);

            then(userService).should().getById(followeeId);
            then(followService).should().getFollowerCount(followeeId);
        }

        @Test
        @DisplayName("팔로워가 없으면 0 반환")
        void withNoFollowers_returnsZero() {
            // given
            UUID followeeId = UUID.randomUUID();
            UserModel user = UserModelFixture.builder()
                .set("id", followeeId)
                .sample();

            given(userService.getById(followeeId)).willReturn(user);
            given(followService.getFollowerCount(followeeId)).willReturn(0L);

            // when
            long result = followFacade.getFollowerCount(followeeId);

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
            UserModel follower = UserModelFixture.builder()
                .set("id", followerId)
                .sample();
            UserModel followee = UserModelFixture.builder()
                .set("id", followeeId)
                .sample();

            given(userService.getById(followerId)).willReturn(follower);
            given(userService.getById(followeeId)).willReturn(followee);
            given(followService.isFollow(followerId, followeeId)).willReturn(true);

            // when
            boolean result = followFacade.isFollow(followerId, followeeId);

            // then
            assertThat(result).isTrue();

            then(userService).should().getById(followerId);
            then(userService).should().getById(followeeId);
            then(followService).should().isFollow(followerId, followeeId);
        }

        @Test
        @DisplayName("팔로우 관계가 없으면 false 반환")
        void withNoFollow_returnsFalse() {
            // given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            UserModel follower = UserModelFixture.builder()
                .set("id", followerId)
                .sample();
            UserModel followee = UserModelFixture.builder()
                .set("id", followeeId)
                .sample();

            given(userService.getById(followerId)).willReturn(follower);
            given(userService.getById(followeeId)).willReturn(followee);
            given(followService.isFollow(followerId, followeeId)).willReturn(false);

            // when
            boolean result = followFacade.isFollow(followerId, followeeId);

            // then
            assertThat(result).isFalse();
        }
    }
}
