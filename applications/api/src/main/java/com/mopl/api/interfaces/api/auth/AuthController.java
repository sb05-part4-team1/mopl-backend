package com.mopl.api.interfaces.api.auth;

import com.mopl.api.application.auth.AuthFacade;
import com.mopl.security.jwt.dto.JwtResponse;
import com.mopl.security.jwt.service.TokenRefreshService;
import com.mopl.security.jwt.service.TokenRefreshService.TokenRefreshResult;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiSpec {

    private final TokenRefreshService tokenRefreshService;
    private final AuthFacade authFacade;

    @GetMapping("/csrf-token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void getCsrfToken(CsrfToken csrfToken) {
    }

    @PostMapping("/refresh")
    public JwtResponse refresh(
        @CookieValue("REFRESH_TOKEN") String refreshToken,
        HttpServletResponse response
    ) {
        TokenRefreshResult result = tokenRefreshService.refresh(refreshToken);
        response.addCookie(result.refreshTokenCookie());
        return result.jwtResponse();
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authFacade.resetPassword(request.email());
    }
}
