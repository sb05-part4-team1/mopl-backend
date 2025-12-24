package com.mopl.jpa.entity.user;

import com.mopl.domain.model.user.AuthProvider;
import com.mopl.domain.model.user.Role;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.base.BaseUpdatableEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserEntity 단위 테스트")
class UserEntityTest {

    @Nested
    @DisplayName("from()")
    class FromTest {

        @Test
        @DisplayName("UserModel로부터 UserEntity 생성")
        void withUserModel_createsUserEntity() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();
            Instant deletedAt = null;

            UserModel userModel = UserModel.builder()
                .id(id)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .authProvider(AuthProvider.EMAIL)
                .email("test@example.com")
                .name("홍길동")
                .password("encodedPassword")
                .profileImageUrl("https://example.com/profile.jpg")
                .role(Role.USER)
                .locked(false)
                .build();

            // when
            UserEntity entity = UserEntity.from(userModel);

            // then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(entity.getDeletedAt()).isNull();
            assertThat(entity.getAuthProvider()).isEqualTo(AuthProvider.EMAIL);
            assertThat(entity.getEmail()).isEqualTo("test@example.com");
            assertThat(entity.getName()).isEqualTo("홍길동");
            assertThat(entity.getPassword()).isEqualTo("encodedPassword");
            assertThat(entity.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
            assertThat(entity.getRole()).isEqualTo(Role.USER);
            assertThat(entity.isLocked()).isFalse();
        }

        @Test
        @DisplayName("BaseUpdatableEntity를 상속함")
        void withUserModel_inheritsBaseUpdatableEntity() {
            // given
            UserModel userModel = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "password"
            );

            // when
            UserEntity entity = UserEntity.from(userModel);

            // then
            assertThat(entity).isInstanceOf(BaseUpdatableEntity.class);
        }
    }

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("UserEntity로부터 UserModel 생성")
        void withUserEntity_createsUserModel() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();
            Instant deletedAt = null;

            UserEntity entity = UserEntity.builder()
                .id(id)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .authProvider(AuthProvider.GOOGLE)
                .email("google@example.com")
                .name("김철수")
                .password("encodedPassword")
                .profileImageUrl("https://example.com/google-profile.jpg")
                .role(Role.ADMIN)
                .locked(true)
                .build();

            // when
            UserModel model = entity.toModel();

            // then
            assertThat(model.getId()).isEqualTo(id);
            assertThat(model.getCreatedAt()).isEqualTo(createdAt);
            assertThat(model.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(model.getDeletedAt()).isNull();
            assertThat(model.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(model.getEmail()).isEqualTo("google@example.com");
            assertThat(model.getName()).isEqualTo("김철수");
            assertThat(model.getPassword()).isEqualTo("encodedPassword");
            assertThat(model.getProfileImageUrl()).isEqualTo("https://example.com/google-profile.jpg");
            assertThat(model.getRole()).isEqualTo(Role.ADMIN);
            assertThat(model.isLocked()).isTrue();
        }
    }

    @Nested
    @DisplayName("from() -> toModel() 왕복 변환")
    class RoundTripTest {

        @Test
        @DisplayName("UserModel -> UserEntity -> UserModel 변환 시 데이터 유지")
        void withRoundTrip_preservesData() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();

            UserModel originalModel = UserModel.builder()
                .id(id)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(null)
                .authProvider(AuthProvider.KAKAO)
                .email("kakao@example.com")
                .name("이영희")
                .password("encodedPassword")
                .profileImageUrl("https://example.com/kakao-profile.jpg")
                .role(Role.USER)
                .locked(false)
                .build();

            // when
            UserEntity entity = UserEntity.from(originalModel);
            UserModel convertedModel = entity.toModel();

            // then
            assertThat(convertedModel.getId()).isEqualTo(originalModel.getId());
            assertThat(convertedModel.getCreatedAt()).isEqualTo(originalModel.getCreatedAt());
            assertThat(convertedModel.getUpdatedAt()).isEqualTo(originalModel.getUpdatedAt());
            assertThat(convertedModel.getDeletedAt()).isEqualTo(originalModel.getDeletedAt());
            assertThat(convertedModel.getAuthProvider()).isEqualTo(originalModel.getAuthProvider());
            assertThat(convertedModel.getEmail()).isEqualTo(originalModel.getEmail());
            assertThat(convertedModel.getName()).isEqualTo(originalModel.getName());
            assertThat(convertedModel.getPassword()).isEqualTo(originalModel.getPassword());
            assertThat(convertedModel.getProfileImageUrl()).isEqualTo(originalModel.getProfileImageUrl());
            assertThat(convertedModel.getRole()).isEqualTo(originalModel.getRole());
            assertThat(convertedModel.isLocked()).isEqualTo(originalModel.isLocked());
        }
    }
}
