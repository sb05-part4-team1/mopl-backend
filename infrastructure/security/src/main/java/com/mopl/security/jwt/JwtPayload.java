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
    String email,
    Set<UserModel.Role> roles,
    String jti,
    TokenType type
) {

    public static final String CLAIM_USER_ID = "userId";
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
        String email,
        Set<UserModel.Role> roles,
        TokenType type
    ) {
        if (userId == null) {
            throw new InvalidTokenException("토큰에 사용자 ID가 포함되어 있지 않습니다.");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidTokenException("토큰에 이메일이 포함되어 있지 않습니다.");
        }
        if (roles == null || roles.isEmpty()) {
            throw new InvalidTokenException("토큰에 사용자 권한이 포함되어 있지 않습니다.");
        }
        if (type == null) {
            throw new InvalidTokenException("토큰에 타입이 포함되어 있지 않습니다.");
        }
        return new JwtPayload(userId, email, roles, UUID.randomUUID().toString(), type);
    }

    public static JwtPayload from(JWTClaimsSet claims) throws ParseException {
        String rawUserId = claims.getStringClaim(CLAIM_USER_ID);
        String subject = claims.getSubject();
        String jti = claims.getJWTID();
        String rawType = claims.getStringClaim(CLAIM_TYPE);

        validateRequiredClaims(rawUserId, subject, jti, rawType);

        try {
            return new JwtPayload(
                UUID.fromString(rawUserId),
                subject,
                extractRoles(claims),
                jti,
                TokenType.from(rawType)
            );
        } catch (InvalidTokenException invalidTokenException) {
            throw invalidTokenException;
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException(
                "토큰 데이터 형식이 유효하지 않습니다.",
                Map.of("userId", Objects.toString(rawUserId, "null"))
            );
        } catch (Exception e) {
            throw new InvalidTokenException("토큰 정보를 읽는 중 오류가 발생했습니다.");
        }
    }

    private static void validateRequiredClaims(String userId, String email, String jti, String type) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidTokenException("토큰에 사용자 ID가 없습니다.");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidTokenException("토큰에 이메일이 없습니다.");
        }
        if (jti == null || jti.isBlank()) {
            throw new InvalidTokenException("토큰에 JTI가 없습니다.");
        }
        if (type == null || type.isBlank()) {
            throw new InvalidTokenException("토큰에 타입이 없습니다.");
        }
    }

    private static Set<UserModel.Role> extractRoles(JWTClaimsSet claims) throws ParseException {
        List<String> roleStrings = claims.getStringListClaim(CLAIM_ROLES);
        if (roleStrings == null || roleStrings.isEmpty()) {
            return Collections.emptySet();
        }

        return roleStrings.stream()
            .map(role -> {
                try {
                    return UserModel.Role.valueOf(role);
                } catch (IllegalArgumentException e) {
                    throw new InvalidTokenException("알 수 없는 권한입니다.", Map.of("role", role));
                }
            })
            .collect(Collectors.toUnmodifiableSet());
    }
}
