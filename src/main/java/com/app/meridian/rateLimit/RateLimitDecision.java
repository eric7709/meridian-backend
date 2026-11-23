package com.app.meridian.rateLimit;

public record RateLimitDecision(boolean allowed, int limit, long remaining, long retryAfterSeconds) {
}
