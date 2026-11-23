package com.app.meridian.rateLimit;

import java.time.Duration;

/** Fixed-window counter per client. Implementations must never throw: a broken limiter fails open. */
public interface RateLimiter {

    RateLimitDecision tryConsume(String clientKey, String scope, int limit, Duration window);
}
