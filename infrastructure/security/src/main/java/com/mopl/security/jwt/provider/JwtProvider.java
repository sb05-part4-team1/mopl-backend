package com.mopl.security.jwt.provider;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.logging.context.LogContext;
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

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

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

    public JwtInformation issueTokenPair(UUID userId, UserModel.Role role) {
        IssuedToken accessToken = generateToken(userId, role, TokenType.ACCESS);
        IssuedToken refreshToken = generateToken(userId, role, TokenType.REFRESH);

        return new JwtInformation(
            accessToken.encoded(),
            refreshToken.encoded(),
            accessToken.payload(),
            refreshToken.payload()
        );
    }

    public JwtPayload verifyAndParse(String token, TokenType expectedType) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            verifySignature(signedJWT, expectedType);

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            validateExpiration(claims.getExpirationTime());

            return extractPayload(claims);
        } catch (ParseException | JOSEException e) {
            throw InvalidTokenException.create();
        }
    }

    private record IssuedToken(String encoded, JwtPayload payload) {
    }

    private IssuedToken generateToken(UUID userId, UserModel.Role role, TokenType tokenType) {
        try {
            UUID jti = UUID.randomUUID();
            Date iat = new Date();
            Date exp = new Date(iat.getTime() + expirations.get(tokenType).toMillis());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .jwtID(jti.toString())
                .issueTime(iat)
                .expirationTime(exp)
                .claim("role", role.name())
                .build();

            SignedJWT signedJWT = new SignedJWT(JWS_HEADER, claimsSet);
            signedJWT.sign(signers.get(tokenType));

            String encoded = signedJWT.serialize();
            JwtPayload payload = new JwtPayload(userId, jti, iat, exp, role);

            return new IssuedToken(encoded, payload);
        } catch (JOSEException e) {
            LogContext.with("tokenType", tokenType).and("userId", userId).error("Token generation failed", e);
            throw new IllegalStateException("토큰 발행 중 오류가 발생했습니다.", e);
        }
    }

    private void verifySignature(SignedJWT jwt, TokenType type) throws JOSEException {
        List<JWSVerifier> typeVerifiers = verifiers.get(type);

        for (int i = 0; i < typeVerifiers.size(); i++) {
            if (jwt.verify(typeVerifiers.get(i))) {
                if (i > 0) {
                    LogContext.with("tokenType", type).info("Token verified with previous secret (Key Rotation)");
                }
                return;
            }
        }
        throw InvalidTokenException.create();
    }

    private JwtPayload extractPayload(JWTClaimsSet claims) throws ParseException {
        return new JwtPayload(
            parseUuidClaim(claims.getSubject(), "sub"),
            parseUuidClaim(claims.getJWTID(), "jti"),
            claims.getIssueTime(),
            claims.getExpirationTime(),
            parseRoleClaim(claims.getStringClaim("role"))
        );
    }

    private void validateExpiration(Date expiration) {
        if (expiration == null || expiration.before(new Date())) {
            throw InvalidTokenException.create();
        }
    }

    @SuppressWarnings("unused")
    private UUID parseUuidClaim(String value, String claimName) {
        if (!hasText(value)) {
            throw InvalidTokenException.create();
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw InvalidTokenException.create();
        }
    }

    private UserModel.Role parseRoleClaim(String value) {
        if (!hasText(value)) {
            throw InvalidTokenException.create();
        }
        try {
            return UserModel.Role.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw InvalidTokenException.create();
        }
    }

    private void initializeTokenConfig(
        TokenType type,
        JwtProperties.Config config
    ) throws JOSEException {
        validateSecret(config.secret());
        signers.put(type, new MACSigner(toBytes(config.secret())));
        verifiers.put(type, createVerifiers(config, type));
        expirations.put(type, config.expiration());
    }

    private List<JWSVerifier> createVerifiers(
        JwtProperties.Config config,
        TokenType type
    ) throws JOSEException {
        List<JWSVerifier> list = new ArrayList<>();
        list.add(new MACVerifier(toBytes(config.secret())));

        if (hasText(config.previousSecret())) {
            validateSecret(config.previousSecret());
            list.add(new MACVerifier(toBytes(config.previousSecret())));
            LogContext.with("tokenType", type).info("Key rotation verifier enabled");
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
