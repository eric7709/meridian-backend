package com.app.meridian.rateLimit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.app.meridian.exceptions.RateLimitExceededException;
import com.app.meridian.security.UserPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * One budget per signed-in account, or per IP for anonymous callers. Auth endpoints get a much
 * smaller budget so password guessing is expensive. Every response advertises what is left.
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String AUTH_PATH = "/api/meridian/auth/";

    /** Only these auth endpoints get the brute-force budget; /auth/me is an ordinary read. */
    private static final java.util.Set<String> SENSITIVE = java.util.Set.of(
            "/login", "/refresh", "/forgot-password", "/reset-password");

    private final RateLimiter rateLimiter;
    private final RateLimitProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!properties.enabled()) {
            return true;
        }
        String uri = request.getRequestURI();
        boolean authScope = uri.startsWith(AUTH_PATH) && SENSITIVE.stream().anyMatch(uri::endsWith);
        int limit = authScope ? properties.authRequestsPerMinute() : properties.requestsPerMinute();
        RateLimitDecision decision = rateLimiter.tryConsume(clientKey(request),
                authScope ? "auth" : "api", limit, properties.window());
        response.setHeader("X-RateLimit-Limit", String.valueOf(decision.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
        if (!decision.allowed()) {
            // the exception handler adds Retry-After to the 429 response itself
            throw new RateLimitExceededException(
                    "Too many requests — try again in " + decision.retryAfterSeconds() + "s",
                    decision.retryAfterSeconds());
        }
        return true;
    }

    /** The account id when signed in, otherwise the caller's address (honouring a proxy header). */
    private String clientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return "user:" + principal.id();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        String address = forwarded != null && !forwarded.isBlank()
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
        return "ip:" + address;
    }
}
