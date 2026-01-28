package com.mopl.logging.mdc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.mopl.logging.mdc.MdcKeys.USER_ID;

/**
 * Spring Security 필터 이후에 실행되어 userId를 MDC에 설정합니다.
 * SecurityFilterChain 등록 시 addFilterAfter로 추가해야 합니다.
 */
public class MdcUserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        setUserIdFromSecurityContext();
        filterChain.doFilter(request, response);
    }

    private void setUserIdFromSecurityContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                MDC.put(USER_ID, authentication.getName());
            }
        } catch (Exception ignored) {
            // Security not available or not authenticated
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/favicon");
    }
}
