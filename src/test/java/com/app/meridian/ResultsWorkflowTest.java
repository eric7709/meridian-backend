package com.app.meridian;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

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
 * Walks the whole results workflow over HTTP against the seeded database: draft, score entry,
 * submission, rejection rules, approval, then the student's read-only view.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ResultsWorkflowTest {

    private static final String PASSWORD = "Password@123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void resultsWorkflowFromDraftToPublished() throws Exception {
        String admin = login("admin@meridian.test");
        String teacher = login("teacher@meridian.test");
        String student = login("student1@meridian.test");

        long term = firstId("/api/meridian/terms?isActive=true", admin);
        long assignment = firstId("/api/meridian/assignments", admin);

        // 1. a teacher opens a draft sheet
        JsonNode sheet = body(mvc.perform(post("/api/meridian/result-sheets")
                .header("Authorization", "Bearer " + teacher)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"assignmentId\":" + assignment + ",\"termId\":" + term + "}"))
                .andReturn());
        long sheetId = sheet.get("id").asLong();
        assertThat(sheet.get("status").asText()).isEqualTo("DRAFT");
        assertThat(sheet.get("code").asText()).startsWith("RES-");

        // 2. the same sheet cannot be opened twice
        mvc.perform(post("/api/meridian/result-sheets")
                .header("Authorization", "Bearer " + teacher)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"assignmentId\":" + assignment + ",\"termId\":" + term + "}"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(409));

        // 3. CA is capped at 40 and the exam at 60
        score(teacher, sheetId, 1, "{\"ca\":55,\"exam\":10}", 400);
        score(teacher, sheetId, 1, "{\"ca\":10,\"exam\":75}", 400);

        // 4. a valid pair computes the total and looks the grade up from the scale
        JsonNode saved = body(mvc.perform(put("/api/meridian/result-sheets/" + sheetId + "/scores/1")
                .header("Authorization", "Bearer " + teacher)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ca\":32,\"exam\":48}"))
                .andReturn());
        assertThat(saved.get("total").asDouble()).isEqualTo(80.0);
        assertThat(saved.get("gradeCode").asText()).isEqualTo("A1");
        score(teacher, sheetId, 2, "{\"ca\":28,\"exam\":40}", 200);

        // 5. the roster reports marked / not marked counts for the progress bar
        JsonNode roster = body(mvc.perform(get("/api/meridian/result-sheets/" + sheetId + "/scores")
                .header("Authorization", "Bearer " + teacher)).andReturn());
        assertThat(roster.get("total").asInt()).isEqualTo(2);
        assertThat(roster.get("marked").asInt()).isEqualTo(2);
        assertThat(roster.get("notMarked").asInt()).isZero();
        assertThat(roster.get("highest").asDouble()).isEqualTo(80.0);

        // 6. teachers cannot approve their own work
        score(teacher, sheetId, 1, "{}", 400);
        assertThat(mvc.perform(patch("/api/meridian/result-sheets/" + sheetId + "/approve")
                .header("Authorization", "Bearer " + teacher))
                .andReturn().getResponse().getStatus()).isEqualTo(403);

        // 7. submitting moves the sheet to PENDING
        JsonNode submitted = body(mvc.perform(post("/api/meridian/result-sheets/" + sheetId + "/submit")
                .header("Authorization", "Bearer " + teacher)).andReturn());
        assertThat(submitted.get("status").asText()).isEqualTo("PENDING");

        // 8. an approved sheet can no longer be edited
        score(teacher, sheetId, 1, "{\"ca\":10,\"exam\":10}", 400);

        // 9. admin approval publishes it
        JsonNode approved = body(mvc.perform(patch("/api/meridian/result-sheets/" + sheetId + "/approve")
                .header("Authorization", "Bearer " + admin)).andReturn());
        assertThat(approved.get("status").asText()).isEqualTo("APPROVED");
        assertThat(approved.get("reviewedByName").asText()).isNotBlank();

        // 10. the student now sees exactly one subject, with the position computed from the class
        JsonNode results = body(mvc.perform(get("/api/meridian/students/me/results?term=FIRST")
                .header("Authorization", "Bearer " + student)).andReturn());
        JsonNode subjects = results.get("terms").get(0).get("subjects");
        assertThat(subjects).hasSize(1);
        assertThat(subjects.get(0).get("grade").asText()).isEqualTo("A1");
        assertThat(subjects.get(0).get("position").asInt()).isEqualTo(1);

        // 11. term reports were recomputed on approval
        JsonNode reports = body(mvc.perform(get("/api/meridian/students/me/reports")
                .header("Authorization", "Bearer " + student)).andReturn());
        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).get("classPosition").asInt()).isEqualTo(1);
        assertThat(reports.get(0).get("classSize").asInt()).isEqualTo(2);

        // 12. the report card downloads as a PDF
        MvcResult pdf = mvc.perform(get("/api/meridian/students/me/report-card.pdf?termId=" + term)
                .header("Authorization", "Bearer " + student)).andReturn();
        assertThat(pdf.getResponse().getStatus()).isEqualTo(200);
        assertThat(pdf.getResponse().getContentType()).isEqualTo("application/pdf");
        assertThat(pdf.getResponse().getContentAsByteArray().length).isGreaterThan(500);

        // 13. students are pinned to their own record
        assertThat(mvc.perform(get("/api/meridian/teachers/me/subjects")
                .header("Authorization", "Bearer " + student))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    void optionalModulesAnswerNotFoundWhenSwitchedOff() throws Exception {
        String admin = login("admin@meridian.test");
        mvc.perform(put("/api/meridian/settings/modules")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"feesEnabled\":false}")).andReturn();
        assertThat(mvc.perform(get("/api/meridian/fees/summary")
                .header("Authorization", "Bearer " + admin))
                .andReturn().getResponse().getStatus()).isEqualTo(404);
        mvc.perform(put("/api/meridian/settings/modules")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"feesEnabled\":true}")).andReturn();
        assertThat(mvc.perform(get("/api/meridian/fees/summary")
                .header("Authorization", "Bearer " + admin))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void badCredentialsAndAnonymousRequestsAreRejected() throws Exception {
        String status = String.valueOf(mvc.perform(post("/api/meridian/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"admin@meridian.test\",\"password\":\"wrong\"}"))
                .andReturn().getResponse().getStatus());
        assertThat(status).isIn("401", "403");
        assertThat(mvc.perform(get("/api/meridian/students"))
                .andReturn().getResponse().getStatus()).isIn(401, 403);
    }

    private void score(String token, long sheetId, long studentId, String payload, int expectedStatus)
            throws Exception {
        int actual = mvc.perform(put("/api/meridian/result-sheets/" + sheetId + "/scores/" + studentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andReturn().getResponse().getStatus();
        assertThat(actual).as("PUT score " + payload).isEqualTo(expectedStatus);
    }

    private String login(String identifier) throws Exception {
        JsonNode node = body(mvc.perform(post("/api/meridian/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"" + identifier + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andReturn());
        return node.get("accessToken").asText();
    }

    private long firstId(String url, String token) throws Exception {
        JsonNode node = body(mvc.perform(get(url).header("Authorization", "Bearer " + token)).andReturn());
        return node.get("data").get(0).get("id").asLong();
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
