package com.mopl.api.interfaces.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.web.csrf.CsrfToken;

public interface AuthApiSpec {

    @Operation(summary = "CSRF 토큰 요청", description = "CSRF 토큰을 쿠키로 발급받습니다.")
    @ApiResponse(responseCode = "204", description = "CSRF 토큰이 쿠키에 설정됨")
    void getCsrfToken(@Parameter(hidden = true) CsrfToken csrfToken);
}
