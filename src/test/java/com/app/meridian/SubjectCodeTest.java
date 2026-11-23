package com.app.meridian;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A subject code is optional: a junior secondary school that has no codes can just post a name and
 * get one generated, while a school that does keep codes can send its own.
 */
@SpringBootTest(properties = "meridian.rate-limit.enabled=false")
@AutoConfigureMockMvc
class SubjectCodeTest {

    private static final String PASSWORD = "Password@123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void codeIsGeneratedWhenTheCallerLeavesItOut() throws Exception {
        String token = login();
        JsonNode created = create(token, "{\"name\":\"Basic Technology\",\"level\":\"JSS\"}");

        assertThat(created.get("name").asText()).isEqualTo("Basic Technology");
        assertThat(created.get("code").asText()).startsWith("SUB/");
        assertThat(created.get("code").asText()).hasSizeGreaterThan(4);
    }

    @Test
    void codeProvidedByTheSchoolIsKept() throws Exception {
        String token = login();
        JsonNode created = create(token, "{\"name\":\"Further Mathematics\",\"code\":\"FM-JSS\",\"level\":\"SS\"}");

        assertThat(created.get("code").asText()).isEqualTo("FM-JSS");
    }

    @Test
    void blankCodeIsTreatedAsOmitted() throws Exception {
        String token = login();
        JsonNode created = create(token, "{\"name\":\"Civic Education\",\"code\":\"  \",\"level\":\"JSS\"}");

        assertThat(created.get("code").asText()).startsWith("SUB/");
    }

    @Test
    void generatedCodesDoNotCollide() throws Exception {
        String token = login();
        String first = create(token, "{\"name\":\"Home Economics\"}").get("code").asText();
        String second = create(token, "{\"name\":\"Agricultural Science\"}").get("code").asText();

        assertThat(first).isNotEqualTo(second);
    }

    private JsonNode create(String token, String json) throws Exception {
        String response = mvc.perform(post("/api/meridian/subjects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String login() throws Exception {
        String response = mvc.perform(post("/api/meridian/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"admin@meridian.test\",\"password\":\"" + PASSWORD + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
