package com.mopl.security.provider.jwt.registry;

import com.mopl.security.provider.jwt.JwtInformation;

import java.util.UUID;

public interface JwtRegistry {
    void registerJwtInformation(JwtInformation jwtInformation);

    // 2. 특정 액세스 토큰 무효화 (로그아웃 시 블랙리스트 등록)
    void invalidateAccessToken(String accessToken);

    // 3. 토큰 갱신 (기존 RT 확인 후 새로운 정보로 교체)
    void rotate(String oldRefreshToken, JwtInformation newJwtInformation);

    // 4. 유효성 검증 (인덱스 기반 O(1) 체크)
    boolean isAccessTokenActive(String accessToken);

    boolean isRefreshTokenActive(String refreshToken);

    // 5. 전체 로그아웃 (사용자의 모든 세션 제거)
    void purgeByUserId(UUID userId);

    // 6. 메모리 정리 (스케줄러용)
    void cleanUp();
}
