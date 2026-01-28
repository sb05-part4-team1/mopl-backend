package com.mopl.test.support;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

/**
 * Utility class for security-related test operations.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @BeforeEach
 * void setUp() {
 *     SecurityTestSupport.authenticateAsUser(userId);
 * }
 *
 * @AfterEach
 * void tearDown() {
 *     SecurityTestSupport.clearAuthentication();
 * }
 * }</pre>
 */
public final class SecurityTestSupport {

    private SecurityTestSupport() {
    }

    /**
     * Sets up authentication for a user with USER role.
     */
    public static void authenticateAsUser(UUID userId) {
        authenticate(userId, "ROLE_USER");
    }

    /**
     * Sets up authentication for an admin with ADMIN role.
     */
    public static void authenticateAsAdmin(UUID userId) {
        authenticate(userId, "ROLE_ADMIN");
    }

    /**
     * Sets up authentication with custom roles.
     */
    public static void authenticate(UUID userId, String... roles) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
            .map(SimpleGrantedAuthority::new)
            .toList();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userId.toString(),
            null,
            authorities
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    /**
     * Clears the security context.
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Gets the currently authenticated user ID.
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        return UUID.fromString(authentication.getPrincipal().toString());
    }
}
