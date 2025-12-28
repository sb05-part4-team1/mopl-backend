package com.mopl.security.jwt;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.model.user.UserModel;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.Getter;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record JwtPayload(
    UUID userId,
    Set<UserModel.Role> roles,
    String jti,
    TokenType type
) {

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_TYPE = "type";

    @Getter
    public enum TokenType {
        ACCESS("access"),
        REFRESH("refresh");

        private final String value;

        TokenType(String value) {
            this.value = value;
        }

        public static TokenType from(String value) {
            return Arrays.stream(values())
                .filter(t -> t.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 토큰 타입입니다.", Map.of("value", value)));
        }
    }

    public static JwtPayload create(
        UUID userId,
        Set<UserModel.Role> roles,
        TokenType type
    ) {
        validate(userId != null, "사용자 ID가 누락되었습니다.");
        validate(roles != null && !roles.isEmpty(), "사용자 권한이 누락되었습니다.");
        validate(type != null, "토큰 타입이 누락되었습니다.");

        return new JwtPayload(userId, roles, UUID.randomUUID().toString(), type);
    }

    public static JwtPayload from(JWTClaimsSet claims) throws ParseException {
        String sub = claims.getSubject();
        String jti = claims.getJWTID();
        String rawType = claims.getStringClaim(CLAIM_TYPE);

        validate(sub != null && !sub.isBlank(), "토큰에 사용자 식별자(subject)가 없습니다.");
        validate(jti != null && !jti.isBlank(), "토큰에 JTI가 없습니다.");
        validate(rawType != null && !rawType.isBlank(), "토큰에 타입이 없습니다.");

        try {
            return new JwtPayload(
                UUID.fromString(sub),
                extractRoles(claims),
                jti,
                TokenType.from(rawType)
            );
        } catch (InvalidTokenException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException(
                "토큰 데이터 형식이 유효하지 않습니다(UUID).",
                Map.of("subject", Objects.toString(sub, "null"))
            );
        } catch (Exception e) {
            throw new InvalidTokenException("토큰 정보를 읽는 중 오류가 발생했습니다.",
                Map.of("errorMessage", Objects.toString(e.getMessage(), "unknown")));
        }
    }

    private static void validate(boolean condition, String message) {
        if (!condition) {
            throw new InvalidTokenException(message);
        }
    }

    private static Set<UserModel.Role> extractRoles(JWTClaimsSet claims) throws ParseException {
        List<String> roleStrings = claims.getStringListClaim(CLAIM_ROLES);
        if (roleStrings == null || roleStrings.isEmpty()) {
            return Collections.emptySet();
        }

        return roleStrings.stream()
            .map(JwtPayload::convertToRole)
            .collect(Collectors.toUnmodifiableSet());
    }

    private static UserModel.Role convertToRole(String roleName) {
        try {
            return UserModel.Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("알 수 없는 권한입니다.", Map.of("role", roleName));
        }
    }
}
