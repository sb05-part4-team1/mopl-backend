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

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtPayload payload = jwtProvider.verifyAndParse(token, TokenType.ACCESS);

            checkBlacklist(payload.jti());

            authenticateUser(payload, request);

            filterChain.doFilter(request, response);

        } catch (InvalidTokenException e) {
            log.debug("JWT 인증 실패: {}", e.getMessage());
            handleAuthenticationException(response, e);
        } catch (Exception e) {
            log.error("JWT 필터 처리 중 예기치 않은 오류 발생", e);
            handleAuthenticationException(
                response,
                new InternalServerException()
            );
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void checkBlacklist(UUID jti) {
        if (jwtRegistry.isAccessTokenInBlacklist(jti)) {
            log.warn("블랙리스트 토큰 접근 시도: jti={}", jti);
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
    }

    private void authenticateUser(JwtPayload payload, HttpServletRequest request) {
        UserDetails userDetails = createUserDetails(payload);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private UserDetails createUserDetails(JwtPayload payload) {
        return MoplUserDetails.builder()
            .userId(payload.sub())
            .role(payload.role())
            .build();
    }

    private void handleAuthenticationException(
        HttpServletResponse response,
        MoplException exception
    ) throws IOException {
        SecurityContextHolder.clearContext();
        apiResponseHandler.writeError(response, exception);
    }
}
