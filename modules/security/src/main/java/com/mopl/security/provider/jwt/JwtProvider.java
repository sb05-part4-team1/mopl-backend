package com.mopl.security.provider.jwt;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.security.config.JwtProperties;
import com.mopl.security.provider.jwt.JwtPayload.TokenType;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Component
@Slf4j
public class JwtProvider {

    private static final int MIN_SECRET_BYTES = 32;
    private static final JWSHeader JWS_HEADER = new JWSHeader(JWSAlgorithm.HS256);

    private final Map<TokenType, JWSSigner> signers;
    private final Map<TokenType, List<JWSVerifier>> verifiers;
    private final Map<TokenType, Duration> expirations;

    public JwtProvider(JwtProperties properties) {
        this.signers = new EnumMap<>(TokenType.class);
        this.verifiers = new EnumMap<>(TokenType.class);
        this.expirations = new EnumMap<>(TokenType.class);

        try {
            initializeTokenConfig(TokenType.ACCESS, properties.accessToken());
            initializeTokenConfig(TokenType.REFRESH, properties.refreshToken());
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT 암호화 엔진 초기화에 실패했습니다.", e);
        }
    }

    public String generateToken(JwtPayload payload) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plus(expirations.get(payload.type()));

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(payload.userId().toString())
                .jwtID(payload.jti().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiry))
                .claim(JwtPayload.CLAIM_ROLES, payload.roles().stream().map(Enum::name).toList())
                .claim(JwtPayload.CLAIM_TYPE, payload.type().getValue())
                .build();

            SignedJWT signedJWT = new SignedJWT(JWS_HEADER, claimsSet);
            signedJWT.sign(signers.get(payload.type()));

            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("{} 토큰 생성 실패: userId={}", payload.type(), payload.userId(), e);
            throw new IllegalStateException("토큰 발행 중 오류가 발생했습니다.", e);
        }
    }

    public JwtPayload verifyAccessToken(String token) {
        return verifyAndParse(token, TokenType.ACCESS);
    }

    public JwtPayload verifyRefreshToken(String token) {
        return verifyAndParse(token, TokenType.REFRESH);
    }

    private JwtPayload verifyAndParse(String token, TokenType expectedType) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            verifySignature(signedJWT, expectedType);

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            validateExpiration(claims.getExpirationTime());

            JwtPayload payload = JwtPayload.from(claims);
            validateTokenType(payload.type(), expectedType);

            return payload;
        } catch (ParseException e) {
            throw new InvalidTokenException("토큰 형식이 올바르지 않습니다.");
        } catch (JOSEException e) {
            throw new InvalidTokenException("토큰 서명 검증에 실패했습니다.");
        }
    }

    private void verifySignature(SignedJWT jwt, TokenType type) throws JOSEException {
        List<JWSVerifier> typeVerifiers = verifiers.get(type);

        for (int i = 0; i < typeVerifiers.size(); i++) {
            if (jwt.verify(typeVerifiers.get(i))) {
                if (i > 0) {
                    log.info("{} 토큰이 이전 시크릿으로 검증되었습니다. (Key Rotation)", type);
                }
                return;
            }
        }
        throw new InvalidTokenException("유효하지 않은 토큰 서명입니다.");
    }

    private void validateExpiration(Date expiration) {
        if (expiration == null || expiration.toInstant().isBefore(Instant.now())) {
            throw new InvalidTokenException("만료된 토큰입니다.");
        }
    }

    private void validateTokenType(TokenType actual, TokenType expected) {
        if (actual != expected) {
            throw new InvalidTokenException("토큰 타입이 일치하지 않습니다.");
        }
    }

    private void initializeTokenConfig(TokenType type, JwtProperties.Config config) throws JOSEException {
        validateSecret(config.secret());
        signers.put(type, new MACSigner(toBytes(config.secret())));
        verifiers.put(type, createVerifiers(config, type));
        expirations.put(type, config.expiration());
    }

    private List<JWSVerifier> createVerifiers(JwtProperties.Config config, TokenType type) throws JOSEException {
        List<JWSVerifier> list = new ArrayList<>();
        list.add(new MACVerifier(toBytes(config.secret())));

        if (hasText(config.previousSecret())) {
            validateSecret(config.previousSecret());
            list.add(new MACVerifier(toBytes(config.previousSecret())));
            log.info("{} 토큰 Key Rotation 검증기가 활성화되었습니다.", type);
        }

        return List.copyOf(list);
    }

    private void validateSecret(String secret) {
        if (!hasText(secret) || toBytes(secret).length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException(
                "JWT 시크릿 키는 최소 " + MIN_SECRET_BYTES + "바이트 이상이어야 합니다."
            );
        }
    }

    private byte[] toBytes(String secret) {
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
