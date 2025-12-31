package com.mopl.api.interfaces.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApiControllerAdviceTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiControllerAdvice.class)
@DisplayName("ApiControllerAdvice 슬라이스 테스트")
public class ApiControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("1. 라우팅")
    class RoutingTest {

        @Test
        @DisplayName("존재하지 않는 엔드포인트 요청 시 404 Not Found 응답")
        void withNonExistentEndpoint_returns404() throws Exception {
            mockMvc.perform(get("/non-existent"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("허용되지 않는 HTTP 메서드 요청 시 405 Method Not Allowed 응답")
        void withUnsupportedMethod_returns405() throws Exception {
            mockMvc.perform(delete("/test"))
                .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("지원하지 않는 Content-Type 요청 시 415 Unsupported Media Type 응답")
        void withUnsupportedMediaType_returns415() throws Exception {
            mockMvc.perform(post("/test/body")
                .contentType(MediaType.APPLICATION_XML)
                .content("<data/>"))
                .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("허용되지 않는 Accept 헤더 요청 시 406 Not Acceptable 응답")
        void withNotAcceptableMediaType_returns406() throws Exception {
            mockMvc.perform(get("/test")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
        }
    }

    @Nested
    @DisplayName("2. 파라미터 바인딩")
    class ParameterBindingTest {

        @Test
        @DisplayName("필수 쿼리 파라미터 누락 시 400 Bad Request 응답")
        void withMissingParameter_returns400() throws Exception {
            mockMvc.perform(get("/test/param"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("필수 파트 누락 시 400 Bad Request 응답")
        void withMissingPart_returns400() throws Exception {
            mockMvc.perform(multipart("/test/part"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("필수 쿠키 누락 시 400 Bad Request 응답")
        void withMissingCookie_returns400() throws Exception {
            mockMvc.perform(get("/test/cookie"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("필수 헤더 누락 시 400 Bad Request 응답")
        void withMissingHeader_returns400() throws Exception {
            mockMvc.perform(get("/test/header"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("잘못된 타입의 파라미터 요청 시 400 Bad Request 응답")
        void withTypeMismatch_returns400() throws Exception {
            mockMvc.perform(get("/test/uuid/invalid-uuid"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("3. 바디 파싱")
    class BodyParsingTest {

        @Test
        @DisplayName("잘못된 JSON 형식 요청 시 400 Bad Request 응답")
        void withInvalidJson_returns400() throws Exception {
            mockMvc.perform(post("/test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("4. 검증")
    class ValidationTest {

        @Test
        @DisplayName("유효하지 않은 요청 본문 시 400 Bad Request 응답")
        void withInvalidBody_returns400() throws Exception {
            mockMvc.perform(post("/test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("제약 조건 위반 시 400 Bad Request 응답")
        void withConstraintViolation_returns400() throws Exception {
            mockMvc.perform(get("/test/validated-param")
                .param("value", "0"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("5. 서버 에러")
    class ServerErrorTest {

        @Test
        @DisplayName("예상치 못한 예외 발생 시 500 Internal Server Error 응답")
        void withUnexpectedException_returns500() throws Exception {
            mockMvc.perform(get("/test/error"))
                .andExpect(status().isInternalServerError());
        }
    }

    @RestController
    @RequestMapping("/test")
    @Validated
    static class TestController {

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public String get() {
            return "ok";
        }

        @PostMapping("/body")
        public String postBody(@Valid @RequestBody TestRequest request) {
            return request.name;
        }

        @GetMapping("/param")
        public String getWithParam(@RequestParam String required) {
            return required;
        }

        @PostMapping("/part")
        public String postWithPart(@RequestPart MultipartFile file) {
            return file.getName();
        }

        @GetMapping("/cookie")
        public String getWithCookie(@CookieValue String sessionId) {
            return sessionId;
        }

        @GetMapping("/header")
        public String getWithHeader(@RequestHeader("X-Custom-Header") String customHeader) {
            return customHeader;
        }

        @GetMapping("/uuid/{id}")
        public String getWithUuid(@PathVariable UUID id) {
            return id.toString();
        }

        @GetMapping("/validated-param")
        public String getWithValidatedParam(@RequestParam @Min(1) int value) {
            return String.valueOf(value);
        }

        @GetMapping("/error")
        public String getError() {
            throw new RuntimeException("Unexpected error");
        }
    }

    record TestRequest(@NotBlank String name) {
    }
}
