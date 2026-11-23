package com.app.meridian.rateLimit;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param enabled                 switch the limiter off entirely (tests, local debugging)
 * @param requestsPerMinute       budget for ordinary API calls, per client
 * @param authRequestsPerMinute   stricter budget for login / password endpoints (brute force)
 * @param window                  the window those budgets apply to
 * @param backend                 "auto" (default: Redis when reachable, else per-instance memory),
 *                                "memory" or "redis"
 */
@ConfigurationProperties(prefix = "meridian.rate-limit")
public record RateLimitProperties(boolean enabled, int requestsPerMinute, int authRequestsPerMinute,
        Duration window, String backend) {

    public RateLimitProperties {
        requestsPerMinute = requestsPerMinute <= 0 ? 240 : requestsPerMinute;
        authRequestsPerMinute = authRequestsPerMinute <= 0 ? 60 : authRequestsPerMinute;
        window = window == null ? Duration.ofMinutes(1) : window;
        backend = backend == null ? "auto" : backend.toLowerCase();
    }

    public boolean isRedisBackend() {
        return "redis".equals(backend);
    }

    public boolean isAutoBackend() {
        return "auto".equals(backend);
    }
}
