package com.mopl.api.config;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.user.UserModel.Role;
import com.mopl.domain.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminInitializer 단위 테스트")
class AdminInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminInitializer adminInitializer;

    private static final String ADMIN_EMAIL = "admin@mopl.com";
    private static final String ADMIN_NAME = "Admin";
    private static final String ADMIN_PASSWORD = "admin1234!";
    private static final String ENCODED_PASSWORD = "encodedPassword";

    @Nested
    @DisplayName("run()")
    class RunTest {

        @Test
        @DisplayName("Admin 계정이 없으면 새로 생성한다")
        void whenAdminNotExists_shouldCreateAdmin() {
            // given
            AdminProperties adminProperties = new AdminProperties(
                true, ADMIN_EMAIL, ADMIN_NAME, ADMIN_PASSWORD
            );
            AdminInitializer initializer = new AdminInitializer(
                userRepository, passwordEncoder, adminProperties
            );

            given(userRepository.existsByEmail(ADMIN_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(ADMIN_PASSWORD)).willReturn(ENCODED_PASSWORD);

            // when
            initializer.run(null);

            // then
            ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
            then(userRepository).should().save(captor.capture());

            UserModel savedAdmin = captor.getValue();
            assertThat(savedAdmin.getEmail()).isEqualTo(ADMIN_EMAIL);
            assertThat(savedAdmin.getName()).isEqualTo(ADMIN_NAME);
            assertThat(savedAdmin.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(savedAdmin.getRole()).isEqualTo(Role.ADMIN);
            assertThat(savedAdmin.isLocked()).isFalse();
        }

        @Test
        @DisplayName("Admin 계정이 이미 존재하면 생성하지 않는다")
        void whenAdminExists_shouldNotCreateAdmin() {
            // given
            AdminProperties adminProperties = new AdminProperties(
                true, ADMIN_EMAIL, ADMIN_NAME, ADMIN_PASSWORD
            );
            AdminInitializer initializer = new AdminInitializer(
                userRepository, passwordEncoder, adminProperties
            );

            given(userRepository.existsByEmail(ADMIN_EMAIL)).willReturn(true);

            // when
            initializer.run(null);

            // then
            then(userRepository).should(never()).save(any(UserModel.class));
        }
    }
}
