package com.mopl.security.util.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.exception.MoplException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtResponseWriter {

    private final ObjectMapper objectMapper;

    public void writeSuccess(HttpServletResponse response, Object body) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    public void writeError(HttpServletResponse response, MoplException exception) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.from(exception);

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
