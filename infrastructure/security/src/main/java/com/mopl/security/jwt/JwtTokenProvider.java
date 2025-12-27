package com.mopl.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final JWSSigner signer;
    private final MACVerifier verifier;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        try {
            byte[] secretKeyBytes = jwtProperties.secretKey().getBytes();
            this.signer = new MACSigner(secretKeyBytes);
            this.verifier = new MACVerifier(secretKeyBytes);
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to initialize JWT signer/verifier", e);
        }
    }

    public String createAccessToken(UUID userId, String email, String role) {
        return createToken(userId, email, role, jwtProperties.accessTokenExpiration().toMillis());
    }

    public String createRefreshToken(UUID userId) {
        return createToken(userId, null, null, jwtProperties.refreshTokenExpiration().toMillis());
    }

    private String createToken(UUID userId, String email, String role, long expirationMillis) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMillis);

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
            .subject(userId.toString())
            .issueTime(Date.from(now))
            .expirationTime(Date.from(expiration));

        if (email != null) {
            claimsBuilder.claim("email", email);
        }
        if (role != null) {
            claimsBuilder.claim("role", role);
        }

        SignedJWT signedJWT = new SignedJWT(
            new JWSHeader(JWSAlgorithm.HS256),
            claimsBuilder.build()
        );

        try {
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(verifier)) {
                return false;
            }
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expiration != null && expiration.after(new Date());
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    public JwtClaims parseToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return new JwtClaims(
                UUID.fromString(claims.getSubject()),
                (String) claims.getClaim("email"),
                (String) claims.getClaim("role")
            );
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public record JwtClaims(UUID userId, String email, String role) {
    }
}
