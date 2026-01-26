package com.mopl.jpa.entity.follow;

import com.mopl.domain.model.follow.FollowModel;
import com.mopl.jpa.entity.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FollowEntityMapper 단위 테스트")
class FollowEntityMapperTest {

    private final FollowEntityMapper mapper = new FollowEntityMapper();

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("FollowEntity를 FollowModel로 변환")
        void withFollowEntity_returnsFollowModel() {
            // given
            UUID id = UUID.randomUUID();
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            Instant createdAt = Instant.now();

            UserEntity follower = UserEntity.builder().id(followerId).build();
            UserEntity followee = UserEntity.builder().id(followeeId).build();

            FollowEntity entity = FollowEntity.builder()
                .id(id)
                .follower(follower)
                .followee(followee)
                .createdAt(createdAt)
                .deletedAt(null)
                .build();

            // when
            FollowModel result = mapper.toModel(entity);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getFollowerId()).isEqualTo(followerId);
            assertThat(result.getFolloweeId()).isEqualTo(followeeId);
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getDeletedAt()).isNull();
            assertThat(result.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            FollowModel result = mapper.toModel(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("FollowModel을 FollowEntity로 변환")
        void withFollowModel_returnsFollowEntity() {
            // given
            UUID id = UUID.randomUUID();
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            Instant createdAt = Instant.now();

            FollowModel model = FollowModel.builder()
                .id(id)
                .followerId(followerId)
                .followeeId(followeeId)
                .createdAt(createdAt)
                .deletedAt(null)
                .build();

            // when
            FollowEntity result = mapper.toEntity(model);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getFollower().getId()).isEqualTo(followerId);
            assertThat(result.getFollowee().getId()).isEqualTo(followeeId);
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            FollowEntity result = mapper.toEntity(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("followerId가 null인 경우 follower도 null")
        void withNullFollowerId_returnsNullFollower() {
            // given
            UUID id = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();

            FollowModel model = FollowModel.builder()
                .id(id)
                .followerId(null)
                .followeeId(followeeId)
                .build();

            // when
            FollowEntity result = mapper.toEntity(model);

            // then
            assertThat(result.getFollower()).isNull();
            assertThat(result.getFollowee().getId()).isEqualTo(followeeId);
        }

        @Test
        @DisplayName("followeeId가 null인 경우 followee도 null")
        void withNullFolloweeId_returnsNullFollowee() {
            // given
            UUID id = UUID.randomUUID();
            UUID followerId = UUID.randomUUID();

            FollowModel model = FollowModel.builder()
                .id(id)
                .followerId(followerId)
                .followeeId(null)
                .build();

            // when
            FollowEntity result = mapper.toEntity(model);

            // then
            assertThat(result.getFollower().getId()).isEqualTo(followerId);
            assertThat(result.getFollowee()).isNull();
        }
    }
}
