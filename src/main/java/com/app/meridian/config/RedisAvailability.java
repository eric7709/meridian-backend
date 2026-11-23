package com.app.meridian.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * One cheap probe at startup: is there a Redis server where we expect it? Cache and rate limiter both
 * ask this instead of guessing, so the app uses Redis when it is there and quietly falls back to
 * in-process state when it is not. The result is remembered — this is not a health check.
 */
@Slf4j
@Component
public class RedisAvailability {

    private static final int PROBE_TIMEOUT_MILLIS = 300;

    private final String host;
    private final int port;
    private volatile Boolean available;

    public RedisAvailability(@Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port) {
        this.host = host;
        this.port = port;
    }

    public boolean isAvailable() {
        Boolean cached = available;
        if (cached == null) {
            synchronized (this) {
                cached = available;
                if (cached == null) {
                    cached = probe();
                    available = cached;
                }
            }
        }
        return cached;
    }

    private boolean probe() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), PROBE_TIMEOUT_MILLIS);
            log.info("Redis reachable at {}:{} — cache and rate limiting will use it", host, port);
            return true;
        } catch (IOException | RuntimeException ex) {
            log.info("No Redis at {}:{} ({}) — falling back to in-memory cache and rate limiter",
                    host, port, ex.getClass().getSimpleName());
            return false;
        }
    }
}
