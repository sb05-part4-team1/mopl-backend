package com.mopl.jpa.entity.user;

import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserEntityMapper 단위 테스트")
class UserEntityMapperTest {

    private final UserEntityMapper mapper = new UserEntityMapper();

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("UserEntity를 UserModel로 변환")
        void withUserEntity_returnsUserModel() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            String email = "test@example.com";
            String name = "test";
            String password = "encodedPassword";
            String profileImageUrl = "https://example.com/image.png";

            UserEntity userEntity = UserEntity.builder()
                .id(id)
                .createdAt(now)
                .deletedAt(null)
                .updatedAt(now)
                .authProvider(UserModel.AuthProvider.EMAIL)
                .email(email)
                .name(name)
                .password(password)
                .profileImageUrl(profileImageUrl)
                .role(UserModel.Role.USER)
                .locked(false)
                .build();

            // when
            UserModel result = mapper.toModel(userEntity);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getDeletedAt()).isNull();
            assertThat(result.getUpdatedAt()).isEqualTo(now);
            assertThat(result.getAuthProvider()).isEqualTo(UserModel.AuthProvider.EMAIL);
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getPassword()).isEqualTo(password);
            assertThat(result.getProfileImageUrl()).isEqualTo(profileImageUrl);
            assertThat(result.getRole()).isEqualTo(UserModel.Role.USER);
            assertThat(result.isLocked()).isFalse();
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            UserModel result = mapper.toModel(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("UserModel을 UserEntity로 변환")
        void withUserModel_returnsUserEntity() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            String email = "test@example.com";
            String name = "test";
            String password = "encodedPassword";
            String profileImageUrl = "https://example.com/image.png";

            UserModel userModel = UserModel.builder()
                .id(id)
                .createdAt(now)
                .deletedAt(null)
                .updatedAt(now)
                .authProvider(UserModel.AuthProvider.EMAIL)
                .email(email)
                .name(name)
                .password(password)
                .profileImageUrl(profileImageUrl)
                .role(UserModel.Role.USER)
                .locked(false)
                .build();

            // when
            UserEntity result = mapper.toEntity(userModel);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getDeletedAt()).isNull();
            assertThat(result.getUpdatedAt()).isEqualTo(now);
            assertThat(result.getAuthProvider()).isEqualTo(UserModel.AuthProvider.EMAIL);
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getPassword()).isEqualTo(password);
            assertThat(result.getProfileImageUrl()).isEqualTo(profileImageUrl);
            assertThat(result.getRole()).isEqualTo(UserModel.Role.USER);
            assertThat(result.isLocked()).isFalse();
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            UserEntity result = mapper.toEntity(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("양방향 변환 시 데이터 유지")
        void roundTrip_preservesData() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            UserModel originalModel = UserModel.builder()
                .id(id)
                .createdAt(now)
                .deletedAt(null)
                .updatedAt(now)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .email("test@example.com")
                .name("test")
                .password("encodedPassword")
                .profileImageUrl("https://example.com/image.png")
                .role(UserModel.Role.ADMIN)
                .locked(true)
                .build();

            // when
            UserEntity entity = mapper.toEntity(originalModel);
            UserModel resultModel = mapper.toModel(entity);

            // then
            assertThat(resultModel.getId()).isEqualTo(originalModel.getId());
            assertThat(resultModel.getCreatedAt()).isEqualTo(originalModel.getCreatedAt());
            assertThat(resultModel.getDeletedAt()).isEqualTo(originalModel.getDeletedAt());
            assertThat(resultModel.getUpdatedAt()).isEqualTo(originalModel.getUpdatedAt());
            assertThat(resultModel.getAuthProvider()).isEqualTo(originalModel.getAuthProvider());
            assertThat(resultModel.getEmail()).isEqualTo(originalModel.getEmail());
            assertThat(resultModel.getName()).isEqualTo(originalModel.getName());
            assertThat(resultModel.getPassword()).isEqualTo(originalModel.getPassword());
            assertThat(resultModel.getProfileImageUrl()).isEqualTo(originalModel
                .getProfileImageUrl());
            assertThat(resultModel.getRole()).isEqualTo(originalModel.getRole());
            assertThat(resultModel.isLocked()).isEqualTo(originalModel.isLocked());
        }
    }
}
