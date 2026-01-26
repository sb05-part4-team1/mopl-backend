package com.mopl.jpa.repository.follow;

import com.mopl.domain.model.follow.FollowModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.follow.FollowRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.follow.FollowEntityMapper;
import com.mopl.jpa.entity.user.UserEntityMapper;
import com.mopl.jpa.repository.user.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    FollowRepositoryImpl.class,
    FollowEntityMapper.class,
    UserRepositoryImpl.class,
    UserEntityMapper.class
})
@DisplayName("FollowRepositoryImpl 슬라이스 테스트")
class FollowRepositoryImplTest {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    private UserModel follower;
    private UserModel followee;

    @BeforeEach
    void setUp() {
        follower = userRepository.save(
            UserModel.create(
                "follower@example.com",
                "팔로워",
                "encodedPassword"
            )
        );

        followee = userRepository.save(
            UserModel.create(
                "followee@example.com",
                "팔로이",
                "encodedPassword"
            )
        );
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 팔로우 ID로 조회하면 FollowModel을 반환한다")
        void withExistingId_returnsFollowModel() {
            // given
            FollowModel savedFollow = followRepository.save(
                FollowModel.create(followee.getId(), follower.getId())
            );

            // when
            Optional<FollowModel> foundFollow = followRepository.findById(savedFollow.getId());

            // then
            assertThat(foundFollow).isPresent();
            assertThat(foundFollow.get().getId()).isEqualTo(savedFollow.getId());
            assertThat(foundFollow.get().getFollowerId()).isEqualTo(follower.getId());
            assertThat(foundFollow.get().getFolloweeId()).isEqualTo(followee.getId());
        }

        @Test
        @DisplayName("존재하지 않는 팔로우 ID로 조회하면 빈 Optional을 반환한다")
        void withNonExistingId_returnsEmptyOptional() {
            // given
            UUID nonExistingId = UUID.randomUUID();

            // when
            Optional<FollowModel> foundFollow = followRepository.findById(nonExistingId);

            // then
            assertThat(foundFollow).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByFollowerIdAndFolloweeId()")
    class FindByFollowerIdAndFolloweeIdTest {

        @Test
        @DisplayName("팔로우 관계가 존재하면 FollowModel을 반환한다")
        void withExistingFollow_returnsFollowModel() {
            // given
            FollowModel savedFollow = followRepository.save(
                FollowModel.create(followee.getId(), follower.getId())
            );

            // when
            Optional<FollowModel> foundFollow = followRepository.findByFollowerIdAndFolloweeId(
                follower.getId(), followee.getId()
            );

            // then
            assertThat(foundFollow).isPresent();
            assertThat(foundFollow.get().getId()).isEqualTo(savedFollow.getId());
        }

        @Test
        @DisplayName("팔로우 관계가 없으면 빈 Optional을 반환한다")
        void withNoFollow_returnsEmptyOptional() {
            // when
            Optional<FollowModel> foundFollow = followRepository.findByFollowerIdAndFolloweeId(
                follower.getId(), followee.getId()
            );

            // then
            assertThat(foundFollow).isEmpty();
        }
    }

    @Nested
    @DisplayName("findFollowerIdsByFolloweeId()")
    class FindFollowerIdsByFolloweeIdTest {

        @Test
        @DisplayName("팔로워가 있으면 팔로워 ID 목록을 반환한다")
        void withFollowers_returnsFollowerIds() {
            // given
            UserModel anotherFollower = userRepository.save(
                UserModel.create("another@example.com", "다른팔로워", "encodedPassword")
            );

            followRepository.save(FollowModel.create(followee.getId(), follower.getId()));
            followRepository.save(FollowModel.create(followee.getId(), anotherFollower.getId()));

            // when
            List<UUID> followerIds = followRepository.findFollowerIdsByFolloweeId(followee.getId());

            // then
            assertThat(followerIds).hasSize(2);
            assertThat(followerIds).containsExactlyInAnyOrder(follower.getId(), anotherFollower.getId());
        }

        @Test
        @DisplayName("팔로워가 없으면 빈 목록을 반환한다")
        void withNoFollowers_returnsEmptyList() {
            // when
            List<UUID> followerIds = followRepository.findFollowerIdsByFolloweeId(followee.getId());

            // then
            assertThat(followerIds).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByFolloweeId()")
    class CountByFolloweeIdTest {

        @Test
        @DisplayName("팔로워 수를 반환한다")
        void returnsFollowerCount() {
            // given
            UserModel anotherFollower = userRepository.save(
                UserModel.create("another@example.com", "다른팔로워", "encodedPassword")
            );

            followRepository.save(FollowModel.create(followee.getId(), follower.getId()));
            followRepository.save(FollowModel.create(followee.getId(), anotherFollower.getId()));

            // when
            long count = followRepository.countByFolloweeId(followee.getId());

            // then
            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("팔로워가 없으면 0을 반환한다")
        void withNoFollowers_returnsZero() {
            // when
            long count = followRepository.countByFolloweeId(followee.getId());

            // then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("existsByFollowerIdAndFolloweeId()")
    class ExistsByFollowerIdAndFolloweeIdTest {

        @Test
        @DisplayName("팔로우 관계가 존재하면 true를 반환한다")
        void withExistingFollow_returnsTrue() {
            // given
            followRepository.save(FollowModel.create(followee.getId(), follower.getId()));

            // when
            boolean exists = followRepository.existsByFollowerIdAndFolloweeId(
                follower.getId(), followee.getId()
            );

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("팔로우 관계가 없으면 false를 반환한다")
        void withNoFollow_returnsFalse() {
            // when
            boolean exists = followRepository.existsByFollowerIdAndFolloweeId(
                follower.getId(), followee.getId()
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("반대 방향의 팔로우만 존재하면 false를 반환한다")
        void withReverseFollow_returnsFalse() {
            // given - followee가 follower를 팔로우
            followRepository.save(FollowModel.create(follower.getId(), followee.getId()));

            // when - follower가 followee를 팔로우하는지 확인
            boolean exists = followRepository.existsByFollowerIdAndFolloweeId(
                follower.getId(), followee.getId()
            );

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 팔로우를 저장하고 반환한다")
        void withNewFollow_savesAndReturnsFollow() {
            // given
            FollowModel followModel = FollowModel.create(followee.getId(), follower.getId());

            // when
            FollowModel savedFollow = followRepository.save(followModel);

            // then
            assertThat(savedFollow.getId()).isNotNull();
            assertThat(savedFollow.getFollowerId()).isEqualTo(follower.getId());
            assertThat(savedFollow.getFolloweeId()).isEqualTo(followee.getId());
            assertThat(savedFollow.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("존재하는 팔로우를 삭제한다")
        void withExistingFollow_deletesSuccessfully() {
            // given
            FollowModel savedFollow = followRepository.save(
                FollowModel.create(followee.getId(), follower.getId())
            );

            // when
            followRepository.delete(savedFollow);

            // then
            assertThat(followRepository.findById(savedFollow.getId())).isEmpty();
        }

        @Test
        @DisplayName("삭제 후 existsByFollowerIdAndFolloweeId가 false를 반환한다")
        void afterDelete_existsReturnsFalse() {
            // given
            FollowModel savedFollow = followRepository.save(
                FollowModel.create(followee.getId(), follower.getId())
            );

            // when
            followRepository.delete(savedFollow);

            // then
            assertThat(followRepository.existsByFollowerIdAndFolloweeId(
                follower.getId(), followee.getId()
            )).isFalse();
        }
    }
}
