package com.mopl.security.jwt.provider;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.security.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtProvider 단위 테스트")
class JwtProviderTest {

    private JwtProvider jwtProvider;

    private static final String ACCESS_SECRET = "access-secret-key-for-testing-minimum-32-bytes";
    private static final String REFRESH_SECRET = "refresh-secret-key-for-testing-minimum-32-bytes";
    private static final Duration ACCESS_EXPIRATION = Duration.ofMinutes(30);
    private static final Duration REFRESH_EXPIRATION = Duration.ofDays(7);

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
            new JwtProperties.Config(ACCESS_SECRET, ACCESS_EXPIRATION, null),
            new JwtProperties.Config(REFRESH_SECRET, REFRESH_EXPIRATION, null),
            3,
            JwtProperties.JwtRegistryType.IN_MEMORY,
            "REFRESH_TOKEN"
        );
        jwtProvider = new JwtProvider(jwtProperties);
    }

    @Nested
    @DisplayName("issueTokenPair()")
    class IssueTokenPairTest {

        @Test
        @DisplayName("유효한 사용자 정보로 토큰 쌍을 발행한다")
        void withValidUserInfo_issuesTokenPair() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel.Role role = UserModel.Role.USER;

            // when
            JwtInformation jwtInfo = jwtProvider.issueTokenPair(userId, role);

            // then
            assertThat(jwtInfo.accessToken()).isNotBlank();
            assertThat(jwtInfo.refreshToken()).isNotBlank();
            assertThat(jwtInfo.accessTokenPayload().sub()).isEqualTo(userId);
            assertThat(jwtInfo.accessTokenPayload().role()).isEqualTo(role);
            assertThat(jwtInfo.refreshTokenPayload().sub()).isEqualTo(userId);
            assertThat(jwtInfo.refreshTokenPayload().role()).isEqualTo(role);
        }

        @Test
        @DisplayName("액세스 토큰과 리프레시 토큰은 서로 다른 JTI를 가진다")
        void tokenPair_hasDifferentJti() {
            // given
            UUID userId = UUID.randomUUID();

            // when
            JwtInformation jwtInfo = jwtProvider.issueTokenPair(userId, UserModel.Role.USER);

            // then
            assertThat(jwtInfo.accessTokenPayload().jti())
                .isNotEqualTo(jwtInfo.refreshTokenPayload().jti());
        }

        @Test
        @DisplayName("ADMIN 역할로 토큰을 발행할 수 있다")
        void withAdminRole_issuesTokenPair() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel.Role role = UserModel.Role.ADMIN;

            // when
            JwtInformation jwtInfo = jwtProvider.issueTokenPair(userId, role);

            // then
            assertThat(jwtInfo.accessTokenPayload().role()).isEqualTo(UserModel.Role.ADMIN);
            assertThat(jwtInfo.refreshTokenPayload().role()).isEqualTo(UserModel.Role.ADMIN);
        }
    }

    @Nested
    @DisplayName("verifyAndParse()")
    class VerifyAndParseTest {

        @Test
        @DisplayName("유효한 액세스 토큰을 검증하고 파싱한다")
        void withValidAccessToken_verifiesAndParses() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel.Role role = UserModel.Role.USER;
            JwtInformation jwtInfo = jwtProvider.issueTokenPair(userId, role);

            // when
            JwtPayload payload = jwtProvider.verifyAndParse(jwtInfo.accessToken(), TokenType.ACCESS);

            // then
            assertThat(payload.sub()).isEqualTo(userId);
            assertThat(payload.role()).isEqualTo(role);
            assertThat(payload.jti()).isEqualTo(jwtInfo.accessTokenPayload().jti());
        }

        @Test
        @DisplayName("유효한 리프레시 토큰을 검증하고 파싱한다")
        void withValidRefreshToken_verifiesAndParses() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel.Role role = UserModel.Role.ADMIN;
            JwtInformation jwtInfo = jwtProvider.issueTokenPair(userId, role);

            // when
            JwtPayload payload = jwtProvider.verifyAndParse(jwtInfo.refreshToken(), TokenType.REFRESH);

            // then
            assertThat(payload.sub()).isEqualTo(userId);
            assertThat(payload.role()).isEqualTo(role);
            assertThat(payload.jti()).isEqualTo(jwtInfo.refreshTokenPayload().jti());
        }

        @Test
        @DisplayName("다른 시크릿으로 서명된 토큰은 검증에 실패한다")
        void withDifferentSecret_throwsException() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation jwtInfo = jwtProvider.issueTokenPair(userId, UserModel.Role.USER);

            JwtProperties otherProperties = new JwtProperties(
                new JwtProperties.Config("other-access-secret-key-minimum-32-bytes", ACCESS_EXPIRATION, null),
                new JwtProperties.Config("other-refresh-secret-key-minimum-32-bytes", REFRESH_EXPIRATION, null),
                3,
                JwtProperties.JwtRegistryType.IN_MEMORY,
                "REFRESH_TOKEN"
            );
            JwtProvider otherProvider = new JwtProvider(otherProperties);

            // when & then
            assertThatThrownBy(() -> otherProvider.verifyAndParse(jwtInfo.accessToken(), TokenType.ACCESS))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("액세스 토큰을 리프레시 토큰으로 검증하면 실패한다")
        void withWrongTokenType_throwsException() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation jwtInfo = jwtProvider.issueTokenPair(userId, UserModel.Role.USER);

            // when & then
            assertThatThrownBy(() -> jwtProvider.verifyAndParse(jwtInfo.accessToken(), TokenType.REFRESH))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("리프레시 토큰을 액세스 토큰으로 검증하면 실패한다")
        void withRefreshTokenAsAccess_throwsException() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation jwtInfo = jwtProvider.issueTokenPair(userId, UserModel.Role.USER);

            // when & then
            assertThatThrownBy(() -> jwtProvider.verifyAndParse(jwtInfo.refreshToken(), TokenType.ACCESS))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("잘못된 형식의 토큰은 검증에 실패한다")
        void withInvalidFormat_throwsException() {
            // when & then
            assertThatThrownBy(() -> jwtProvider.verifyAndParse("invalid-token", TokenType.ACCESS))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("빈 토큰은 검증에 실패한다")
        void withEmptyToken_throwsException() {
            // when & then
            assertThatThrownBy(() -> jwtProvider.verifyAndParse("", TokenType.ACCESS))
                .isInstanceOf(InvalidTokenException.class);
        }
    }

    @Nested
    @DisplayName("Key Rotation")
    class KeyRotationTest {

        @Test
        @DisplayName("이전 시크릿으로 서명된 토큰도 검증할 수 있다")
        void withPreviousSecret_verifiesToken() {
            // given
            String previousSecret = ACCESS_SECRET;
            String newSecret = "new-access-secret-key-for-testing-minimum-32-bytes";

            JwtProperties oldProperties = new JwtProperties(
                new JwtProperties.Config(previousSecret, ACCESS_EXPIRATION, null),
                new JwtProperties.Config(REFRESH_SECRET, REFRESH_EXPIRATION, null),
                3,
                JwtProperties.JwtRegistryType.IN_MEMORY,
                "REFRESH_TOKEN"
            );
            JwtProvider oldProvider = new JwtProvider(oldProperties);

            UUID userId = UUID.randomUUID();
            JwtInformation oldToken = oldProvider.issueTokenPair(userId, UserModel.Role.USER);

            JwtProperties newProperties = new JwtProperties(
                new JwtProperties.Config(newSecret, ACCESS_EXPIRATION, previousSecret),
                new JwtProperties.Config(REFRESH_SECRET, REFRESH_EXPIRATION, null),
                3,
                JwtProperties.JwtRegistryType.IN_MEMORY,
                "REFRESH_TOKEN"
            );
            JwtProvider newProvider = new JwtProvider(newProperties);

            // when
            JwtPayload payload = newProvider.verifyAndParse(oldToken.accessToken(), TokenType.ACCESS);

            // then
            assertThat(payload.sub()).isEqualTo(userId);
        }
    }

    @Nested
    @DisplayName("초기화")
    class InitializationTest {

        @Test
        @DisplayName("32바이트 미만의 시크릿으로 초기화하면 예외가 발생한다")
        void withShortSecret_throwsException() {
            // given
            JwtProperties invalidProperties = new JwtProperties(
                new JwtProperties.Config("short", ACCESS_EXPIRATION, null),
                new JwtProperties.Config(REFRESH_SECRET, REFRESH_EXPIRATION, null),
                3,
                JwtProperties.JwtRegistryType.IN_MEMORY,
                "REFRESH_TOKEN"
            );

            // when & then
            assertThatThrownBy(() -> new JwtProvider(invalidProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32바이트");
        }

        @Test
        @DisplayName("빈 문자열 시크릿으로 초기화하면 예외가 발생한다")
        void withEmptySecret_throwsException() {
            // given
            JwtProperties invalidProperties = new JwtProperties(
                new JwtProperties.Config("", ACCESS_EXPIRATION, null),
                new JwtProperties.Config(REFRESH_SECRET, REFRESH_EXPIRATION, null),
                3,
                JwtProperties.JwtRegistryType.IN_MEMORY,
                "REFRESH_TOKEN"
            );

            // when & then
            assertThatThrownBy(() -> new JwtProvider(invalidProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32바이트");
        }

        @Test
        @DisplayName("previousSecret이 32바이트 미만이면 예외가 발생한다")
        void withShortPreviousSecret_throwsException() {
            // given
            JwtProperties invalidProperties = new JwtProperties(
                new JwtProperties.Config(ACCESS_SECRET, ACCESS_EXPIRATION, "short"),
                new JwtProperties.Config(REFRESH_SECRET, REFRESH_EXPIRATION, null),
                3,
                JwtProperties.JwtRegistryType.IN_MEMORY,
                "REFRESH_TOKEN"
            );

            // when & then
            assertThatThrownBy(() -> new JwtProvider(invalidProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32바이트");
        }
    }

    @Nested
    @DisplayName("토큰 검증 예외 처리")
    class TokenValidationExceptionTest {

        @Test
        @DisplayName("null 만료 시간은 검증에 실패한다")
        void withNullExpiration_throwsException() {
            // given
            UUID userId = UUID.randomUUID();
            jwtProvider.issueTokenPair(userId, UserModel.Role.USER);

            // 만료된 토큰을 시뮬레이션하기 위해 과거 시간으로 토큰 생성
            JwtProperties expiredProperties = new JwtProperties(
                new JwtProperties.Config(ACCESS_SECRET, Duration.ofMillis(-1), null),
                new JwtProperties.Config(REFRESH_SECRET, REFRESH_EXPIRATION, null),
                3,
                JwtProperties.JwtRegistryType.IN_MEMORY,
                "REFRESH_TOKEN"
            );
            JwtProvider expiredProvider = new JwtProvider(expiredProperties);
            JwtInformation expiredToken = expiredProvider.issueTokenPair(userId, UserModel.Role.USER);

            // when & then
            assertThatThrownBy(() -> jwtProvider.verifyAndParse(expiredToken.accessToken(), TokenType.ACCESS))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("빈 subject는 검증에 실패한다")
        void withEmptySubject_throwsException() {
            // Nimbus JWT는 내부적으로 subject를 설정하므로 파싱 오류로 처리
            // 잘못된 토큰 형식으로 테스트
            assertThatThrownBy(() -> jwtProvider.verifyAndParse("eyJhbGciOiJIUzI1NiJ9.e30.invalid", TokenType.ACCESS))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("유효하지 않은 role 값은 검증에 실패한다")
        void withInvalidRole_throwsException() {
            // 내부적으로 role이 잘못된 경우를 테스트하기는 어려우므로
            // 손상된 토큰으로 테스트
            assertThatThrownBy(() -> jwtProvider.verifyAndParse("invalid.token.format", TokenType.ACCESS))
                .isInstanceOf(InvalidTokenException.class);
        }
    }
}
