package com.mopl.test.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Utility class for MockMvc testing with common patterns.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @Autowired
 * private MockMvc mockMvc;
 *
 * @Autowired
 *            private ObjectMapper objectMapper;
 *
 * @Test
 *       void testApi() throws Exception {
 *       var support = new MockMvcTestSupport(mockMvc, objectMapper);
 *
 *       support.performGet("/api/users/1")
 *       .andExpect(status().isOk());
 *
 *       support.performPost("/api/users", createRequest)
 *       .andExpect(status().isCreated());
 *       }
 *       }</pre>
 */
public class MockMvcTestSupport {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public MockMvcTestSupport(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url)
            .contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions performGet(String url, Object... uriVars) throws Exception {
        return mockMvc.perform(get(url, uriVars)
            .contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions performPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
    }

    public ResultActions performPost(String url) throws Exception {
        return mockMvc.perform(post(url)
            .contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions performPut(String url, Object body) throws Exception {
        return mockMvc.perform(put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
    }

    public ResultActions performPatch(String url, Object body) throws Exception {
        return mockMvc.perform(patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
    }

    public ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url)
            .contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions performDelete(String url, Object... uriVars) throws Exception {
        return mockMvc.perform(delete(url, uriVars)
            .contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions performWithAuth(MockHttpServletRequestBuilder request, String token) throws Exception {
        return mockMvc.perform(request
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON));
    }
}
