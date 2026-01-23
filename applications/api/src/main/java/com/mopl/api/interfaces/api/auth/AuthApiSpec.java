package com.mopl.api.interfaces.api.auth;

import com.mopl.api.interfaces.api.auth.dto.ResetPasswordRequest;
import com.mopl.domain.exception.ErrorResponse;
import com.mopl.security.jwt.dto.JwtResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;

@Tag(name = "Auth API", description = "보안 API")
public interface AuthApiSpec {

    @Operation(
        summary = "CSRF 토큰 요청",
        description = "CSRF 토큰을 조회합니다. 토큰은 쿠키(XSRF-TOKEN)에 저장됩니다."
    )
    @ApiResponse(
        responseCode = "204",
        description = "CSRF 토큰이 쿠키에 설정됨"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    void getCsrfToken(@Parameter(hidden = true) CsrfToken csrfToken);

    @Operation(
        summary = "토큰 갱신",
        description = "쿠키(REFRESH_TOKEN)에 저장된 리프레시 토큰으로 액세스 토큰과 리프레시 토큰을 재발급합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "토큰 갱신 성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "리프레시 토큰 쿠키 누락",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 리프레시 토큰",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )

    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )

    )
    JwtResponse refresh(
        @Parameter(
            name = "REFRESH_TOKEN",
            description = "Refresh Token (쿠키 방식)",
            required = true,
            in = ParameterIn.COOKIE,
            schema = @Schema(implementation = String.class)
        ) String refreshToken,
        @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
        summary = "비밀번호 초기화",
        description = "임시 비밀번호를 생성하여 이메일로 발송합니다. 임시 비밀번호는 3분간 유효합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "임시 비밀번호 발송 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    void resetPassword(ResetPasswordRequest request);
}
