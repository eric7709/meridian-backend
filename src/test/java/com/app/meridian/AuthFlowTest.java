package com.app.meridian;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The token lifecycle over HTTP: login hands out both tokens, refresh rotates them, a replayed
 * refresh token signs every session out, logout revokes, and forgot/reset actually changes the
 * password and kills the sessions that were open before it.
 */
@SpringBootTest(properties = "meridian.rate-limit.enabled=false")
@AutoConfigureMockMvc
class AuthFlowTest {

    private static final String PASSWORD = "Password@123";
    private static final String NEW_PASSWORD = "NewPassword99";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginReturnsAnAccessAndARefreshToken() throws Exception {
        JsonNode login = body(postJson("/api/meridian/auth/login", credentials("admin@meridian.test", PASSWORD)));

        assertThat(login.get("accessToken").asText()).isNotBlank();
        assertThat(login.get("refreshToken").asText()).isNotBlank();
        assertThat(login.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(login.get("expiresInMinutes").asLong()).isBetween(1L, 120L);
        assertThat(login.get("refreshExpiresInMinutes").asLong()).isGreaterThan(login.get("expiresInMinutes").asLong());
        assertThat(login.get("user").get("email").asText()).isEqualTo("admin@meridian.test");

        // the access token is what the API accepts
        assertThat(mvc.perform(get("/api/meridian/auth/me")
                .header("Authorization", "Bearer " + login.get("accessToken").asText()))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void refreshRotatesThePairAndRejectsTheSpentToken() throws Exception {
        JsonNode login = body(postJson("/api/meridian/auth/login", credentials("teacher@meridian.test", PASSWORD)));
        String firstRefresh = login.get("refreshToken").asText();

        JsonNode refreshed = body(postJson("/api/meridian/auth/refresh", refreshBody(firstRefresh)));
        String secondRefresh = refreshed.get("refreshToken").asText();

        assertThat(refreshed.get("accessToken").asText()).isNotBlank();
        assertThat(secondRefresh).isNotEqualTo(firstRefresh);
        assertThat(mvc.perform(get("/api/meridian/auth/me")
                .header("Authorization", "Bearer " + refreshed.get("accessToken").asText()))
                .andReturn().getResponse().getStatus()).isEqualTo(200);

        // replaying the spent token is treated as theft: 403 and every session dies
        assertThat(status(postJson("/api/meridian/auth/refresh", refreshBody(firstRefresh)))).isEqualTo(403);
        assertThat(status(postJson("/api/meridian/auth/refresh", refreshBody(secondRefresh)))).isEqualTo(403);
    }

    @Test
    void logoutRevokesTheRefreshToken() throws Exception {
        JsonNode login = body(postJson("/api/meridian/auth/login", credentials("student1@meridian.test", PASSWORD)));
        String refresh = login.get("refreshToken").asText();

        assertThat(status(postJson("/api/meridian/auth/logout", refreshBody(refresh)))).isEqualTo(204);
        assertThat(status(postJson("/api/meridian/auth/refresh", refreshBody(refresh)))).isEqualTo(403);
    }

    @Test
    void forgotPasswordResetChangesThePasswordAndRevokesOldSessions() throws Exception {
        String email = "student1@meridian.test";
        String oldRefresh = body(postJson("/api/meridian/auth/login", credentials(email, PASSWORD)))
                .get("refreshToken").asText();

        MvcResult forgot = postJson("/api/meridian/auth/forgot-password", "{\"email\":\"" + email + "\"}");
        assertThat(forgot.getResponse().getStatus()).isEqualTo(202);
        JsonNode forgotBody = body(forgot);
        assertThat(forgotBody.get("resetToken").asText()).isNotBlank();
        assertThat(forgotBody.get("message").asText()).contains("reset link");

        String resetToken = forgotBody.get("resetToken").asText();
        assertThat(status(postJson("/api/meridian/auth/reset-password",
                "{\"token\":\"" + resetToken + "\",\"newPassword\":\"" + NEW_PASSWORD + "\"}"))).isEqualTo(204);

        // single use
        assertThat(status(postJson("/api/meridian/auth/reset-password",
                "{\"token\":\"" + resetToken + "\",\"newPassword\":\"" + NEW_PASSWORD + "\"}"))).isEqualTo(400);
        // old password rejected, new one accepted
        assertThat(status(postJson("/api/meridian/auth/login", credentials(email, PASSWORD)))).isIn(401, 403);
        assertThat(status(postJson("/api/meridian/auth/login", credentials(email, NEW_PASSWORD)))).isEqualTo(200);
        // sessions opened before the reset cannot be refreshed
        assertThat(status(postJson("/api/meridian/auth/refresh", refreshBody(oldRefresh)))).isEqualTo(403);

        // restore the seed password so the other tests still see the documented fixture
        String restoreToken = body(postJson("/api/meridian/auth/forgot-password",
                "{\"email\":\"" + email + "\"}")).get("resetToken").asText();
        assertThat(status(postJson("/api/meridian/auth/reset-password",
                "{\"token\":\"" + restoreToken + "\",\"newPassword\":\"" + PASSWORD + "\"}"))).isEqualTo(204);
        assertThat(status(postJson("/api/meridian/auth/login", credentials(email, PASSWORD)))).isEqualTo(200);
    }

    @Test
    void forgotPasswordIsNeutralForUnknownAddresses() throws Exception {
        MvcResult result = postJson("/api/meridian/auth/forgot-password",
                "{\"email\":\"nobody@meridian.test\"}");
        assertThat(result.getResponse().getStatus()).isEqualTo(202);
        JsonNode payload = body(result);
        assertThat(payload.hasNonNull("resetToken")).isFalse();
        assertThat(payload.get("message").asText()).contains("reset link");
    }

    @Test
    void resetPasswordRejectsWeakPasswords() throws Exception {
        JsonNode forgot = body(postJson("/api/meridian/auth/forgot-password",
                "{\"email\":\"teacher@meridian.test\"}"));
        String resetToken = forgot.get("resetToken").asText();
        assertThat(status(postJson("/api/meridian/auth/reset-password",
                "{\"token\":\"" + resetToken + "\",\"newPassword\":\"short\"}"))).isEqualTo(422);
        // ... and the token is still usable, because a rejected password must not spend it
        assertThat(status(postJson("/api/meridian/auth/reset-password",
                "{\"token\":\"" + resetToken + "\",\"newPassword\":\"Teacher#2026\"}"))).isEqualTo(204);
        // restore the seeded password
        String restore = body(postJson("/api/meridian/auth/forgot-password",
                "{\"email\":\"teacher@meridian.test\"}")).get("resetToken").asText();
        assertThat(status(postJson("/api/meridian/auth/reset-password",
                "{\"token\":\"" + restore + "\",\"newPassword\":\"" + PASSWORD + "\"}"))).isEqualTo(204);
    }

    private MvcResult postJson(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json)).andReturn();
    }

    private int status(MvcResult result) {
        return result.getResponse().getStatus();
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private static String credentials(String identifier, String password) {
        return "{\"identifier\":\"" + identifier + "\",\"password\":\"" + password + "\"}";
    }

    private static String refreshBody(String refreshToken) {
        return "{\"refreshToken\":\"" + refreshToken + "\"}";
    }
}
