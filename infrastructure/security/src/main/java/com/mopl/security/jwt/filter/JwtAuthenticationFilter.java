package com.mopl.security.jwt.filter;

import com.mopl.domain.exception.InternalServerException;
import com.mopl.domain.exception.MoplException;
import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.security.exception.ApiResponseHandler;
import com.mopl.security.jwt.provider.JwtPayload;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.provider.TokenType;
import com.mopl.security.jwt.registry.JwtRegistry;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final JwtRegistry jwtRegistry;
    private final ApiResponseHandler apiResponseHandler;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (hasText(token)) {
            try {
                JwtPayload payload = jwtProvider.verifyAndParse(token, TokenType.ACCESS);
                validateNotBlacklisted(payload.jti());
                authenticateUser(payload, request);
            } catch (InvalidTokenException e) {
                log.debug("JWT 인증 실패: {}", e.getMessage());
                handleAuthenticationException(response, e);
                return;
            } catch (Exception e) {
                log.error("JWT 필터 처리 중 예기치 않은 오류 발생", e);
                handleAuthenticationException(response, new InternalServerException());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void validateNotBlacklisted(UUID jti) {
        if (jwtRegistry.isAccessTokenInBlacklist(jti)) {
            log.warn("블랙리스트 토큰 접근 시도: jti={}", jti);
            throw new InvalidTokenException();
        }
    }

    private void authenticateUser(JwtPayload payload, HttpServletRequest request) {
        UserDetails userDetails = MoplUserDetails.builder()
            .userId(payload.sub())
            .role(payload.role())
            .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleAuthenticationException(
        HttpServletResponse response,
        MoplException exception
    ) throws IOException {
        SecurityContextHolder.clearContext();
        apiResponseHandler.writeError(response, exception);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        return (hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX))
            ? authHeader.substring(BEARER_PREFIX.length())
            : null;
    }
}
