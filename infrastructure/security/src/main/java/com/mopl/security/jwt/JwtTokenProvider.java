package com.mopl.security.jwt;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.security.config.JwtProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mopl.security.jwt.JwtPayload.CLAIM_TYPE;
import static org.springframework.util.StringUtils.hasText;

@Component
@Slf4j
public class JwtTokenProvider {

    private final JWSSigner accessTokenSigner;
    private final JWSSigner refreshTokenSigner;
    private final List<JWSVerifier> accessTokenVerifiers;
    private final List<JWSVerifier> refreshTokenVerifiers;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        try {
            this.accessTokenSigner = createSigner(jwtProperties.accessToken().secret());
            this.refreshTokenSigner = createSigner(jwtProperties.refreshToken().secret());
            this.accessTokenVerifiers = createVerifiers(jwtProperties.accessToken(), JwtPayload.TokenType.ACCESS);
            this.refreshTokenVerifiers = createVerifiers(jwtProperties.refreshToken(), JwtPayload.TokenType.REFRESH);
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT 암호화 엔진 초기화에 실패했습니다.", e);
        }

        this.accessTokenExpiration = jwtProperties.accessToken().expiration();
        this.refreshTokenExpiration = jwtProperties.refreshToken().expiration();
    }

    // --- 토큰 생성 ---
    public String generateToken(JwtPayload payload) {
        try {
            JWSSigner signer = getSigner(payload.type());
            Duration expiration = getExpiration(payload.type());

            Date now = new Date();
            Date expiry = new Date(now.getTime() + expiration.toMillis());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(payload.userId().toString())
                .jwtID(payload.jti())
                .issueTime(now)
                .expirationTime(expiry)
                .claim(JwtPayload.CLAIM_ROLES, payload.roles().stream().map(Enum::name).toList())
                .claim(CLAIM_TYPE, payload.type().getValue())
                .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("{} 토큰 생성 실패: userId={}", payload.type(), payload.userId(), e);
            throw new IllegalStateException("토큰 발행 중 오류 발생", e);
        }
    }

    // --- 토큰 검증 및 파싱 ---
    public JwtPayload verifyAccessToken(String token) {
        return verifyAndParse(token, accessTokenVerifiers, JwtPayload.TokenType.ACCESS);
    }

    public JwtPayload verifyRefreshToken(String token) {
        return verifyAndParse(token, refreshTokenVerifiers, JwtPayload.TokenType.REFRESH);
    }

    private JwtPayload verifyAndParse(String token, List<JWSVerifier> verifiers, JwtPayload.TokenType expectedType) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 1. 서명 검증 (Rotation 지원)
            verifySignature(signedJWT, verifiers, expectedType);

            // 2. 만료 및 타입 검증 후 Payload 반환
            JwtPayload payload = JwtPayload.from(signedJWT.getJWTClaimsSet());
            validatePayload(payload, expectedType, signedJWT.getJWTClaimsSet().getExpirationTime());

            return payload;
        } catch (ParseException e) {
            throw new InvalidTokenException("토큰 형식이 올바르지 않습니다.");
        } catch (JOSEException e) {
            throw new InvalidTokenException("토큰 서명 검증에 실패했습니다.");
        }
    }

    // --- 내부 헬퍼 메서드 ---

    private void verifySignature(SignedJWT jwt, List<JWSVerifier> verifiers, JwtPayload.TokenType type) throws JOSEException {
        for (int i = 0; i < verifiers.size(); i++) {
            if (jwt.verify(verifiers.get(i))) {
                if (i > 0) log.info("JWT {} 토큰이 이전 시크릿으로 검증되었습니다. (Rotation)", type);
                return;
            }
        }
        throw new InvalidTokenException("유효하지 않은 토큰 서명입니다.");
    }

    private void validatePayload(JwtPayload payload, JwtPayload.TokenType expectedType, Date exp) {
        if (payload.type() != expectedType) {
            throw new InvalidTokenException("토큰 타입이 일치하지 않습니다.");
        }
        if (exp == null || exp.before(new Date())) {
            throw new InvalidTokenException("만료된 토큰입니다.");
        }
    }

    private JWSSigner createSigner(String secret) throws JOSEException {
        validateSecret(secret);
        return new MACSigner(secret.getBytes(StandardCharsets.UTF_8));
    }

    private List<JWSVerifier> createVerifiers(JwtProperties.Config config, JwtPayload.TokenType type) throws JOSEException {
        List<JWSVerifier> verifiers = new ArrayList<>();
        verifiers.add(new MACVerifier(config.secret().getBytes(StandardCharsets.UTF_8)));

        if (hasText(config.previousSecret())) {
            validateSecret(config.previousSecret());
            verifiers.add(new MACVerifier(config.previousSecret().getBytes(StandardCharsets.UTF_8)));
            log.info("{} 토큰 로테이션용 검증기가 활성화되었습니다.", type);
        }
        return List.copyOf(verifiers);
    }

    private void validateSecret(String secret) {
        if (!hasText(secret) || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT 시크릿 키는 최소 32바이트 이상이어야 합니다.");
        }
    }

    private JWSSigner getSigner(JwtPayload.TokenType type) {
        return type == JwtPayload.TokenType.ACCESS ? accessTokenSigner : refreshTokenSigner;
    }

    private Duration getExpiration(JwtPayload.TokenType type) {
        return type == JwtPayload.TokenType.ACCESS ? accessTokenExpiration : refreshTokenExpiration;
    }
}
