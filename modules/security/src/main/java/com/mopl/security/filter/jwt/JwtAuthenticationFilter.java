package com.mopl.security.filter.jwt;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
import com.mopl.security.provider.jwt.JwtProvider;
import com.mopl.security.provider.jwt.MoplUserDetails;
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
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final JwtResponseWriter responseWriter;
    private final JwtRegistry jwtRegistry;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(BEARER_PREFIX.length());
            JwtPayload jwtPayload = jwtProvider.verifyAccessToken(token);
            if (!jwtRegistry.isAccessTokenBlacklisted(jwtPayload.jti())) {
                log.debug("유효하지 않은 JWT 토큰");
                responseWriter.writeError(response, new InvalidTokenException("유효하지 않은 토큰입니다."));
                return;
            }

            setAuthentication(jwtPayload.userId(), request);
            log.debug("JWT 인증 설정 완료: userId={}", jwtPayload.userId());
        } catch (Exception e) {
            log.debug("JWT 인증 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            responseWriter.writeError(response, new InvalidTokenException(""));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(UUID userId, HttpServletRequest request) {
        try {
            UserModel user = userService.getById(userId);

            UserDetails userDetails = new MoplUserDetails(
                user.getId(),
                user.getRole()
            );

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UserNotFoundException e) {
            throw new InvalidTokenException("");
        }
    }
}
