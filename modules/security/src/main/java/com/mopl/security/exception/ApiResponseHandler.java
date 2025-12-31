package com.mopl.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.exception.MoplException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class ApiResponseHandler {

    private final ObjectMapper objectMapper;

    // 추후 ErrorCode에서 status 추출
    public void writeError(
        HttpServletResponse response,
        MoplException exception
    ) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.from(exception);
        write(response, HttpServletResponse.SC_UNAUTHORIZED, errorResponse);
    }

    public void writeSuccess(HttpServletResponse response, Object body) throws IOException {
        write(response, HttpServletResponse.SC_OK, body);
    }

    private void write(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
