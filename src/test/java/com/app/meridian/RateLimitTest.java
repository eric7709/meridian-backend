package com.app.meridian;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** The login budget is small on purpose: guess the password four times and you wait. */
@SpringBootTest(properties = {
        "meridian.rateLimit.enabled=true",
        "meridian.rateLimit.auth-requests-per-minute=3",
        "meridian.rateLimit.requests-per-minute=1000"
})
@AutoConfigureMockMvc
class RateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    /** With the Redis backend the counters outlive the JVM, so clear the budget before each test. */
    @BeforeEach
    void resetBudget() {
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            var keys = stringRedisTemplate.keys("ratelimit:*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (RuntimeException ex) {
            // no Redis server: the limiter is in-memory and already starts from zero
        }
    }

    @Test
    void repeatedLoginAttemptsAreThrottled() throws Exception {
        String body = "{\"identifier\":\"admin@meridian.test\",\"password\":\"wrong\"}";
        for (int attempt = 1; attempt <= 3; attempt++) {
            mockMvc.perform(post("/api/meridian/auth/login")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden())
                    .andExpect(header().exists("X-RateLimit-Remaining"));
        }
        mockMvc.perform(post("/api/meridian/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void ordinaryCallsKeepTheirOwnLargerBudget() throws Exception {
        mockMvc.perform(post("/api/meridian/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"admin@meridian.test\",\"password\":\"wrong\"}"))
                .andExpect(header().string("X-RateLimit-Limit", "3"));
    }
}
