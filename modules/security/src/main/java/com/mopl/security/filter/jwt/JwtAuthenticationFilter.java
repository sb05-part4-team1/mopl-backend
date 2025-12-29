package com.mopl.security.filter.jwt;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.security.provider.jwt.JwtPayload;
import com.mopl.security.provider.jwt.JwtProvider;
import com.mopl.security.provider.jwt.MoplUserDetails;
import com.mopl.security.provider.jwt.TokenType;
import com.mopl.security.provider.jwt.registry.JwtRegistry;
import com.mopl.security.util.jwt.JwtResponseWriter;
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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final JwtRegistry jwtRegistry;
    private final JwtResponseWriter jwtResponseWriter;

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
            JwtPayload jwtPayload = jwtProvider.verifyAndParse(token, TokenType.ACCESS);

            if (jwtRegistry.isAccessTokenInBlacklist(jwtPayload.jti())) {
                log.warn("블랙리스트에 등록된 토큰 접근 시도: jti={}", jwtPayload.jti());
                jwtResponseWriter.writeError(response, new InvalidTokenException("로그아웃된 토큰입니다."));
                return;
            }

            setAuthentication(jwtPayload, request);
        } catch (InvalidTokenException e) {
            log.debug("JWT 인증 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            jwtResponseWriter.writeError(response, e);
            return;
        } catch (Exception e) {
            log.error("JWT 필터 처리 중 예기치 않은 오류 발생", e);
            SecurityContextHolder.clearContext();
            jwtResponseWriter.writeError(response, new InvalidTokenException("인증 처리 중 오류가 발생했습니다."));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void setAuthentication(JwtPayload jwtPayload, HttpServletRequest request) {
        UserDetails userDetails = new MoplUserDetails(
            jwtPayload.sub(),
            jwtPayload.role()
        );

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
