package com.app.meridian.auth.services;

import java.time.Duration;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Stand-in for the mail server. Everything a real implementation needs is already here (address,
 * token, lifetime); swapping in {@code JavaMailSender} only changes this class.
 */
@Slf4j
@Service
public class ResetMailer {

    public void sendResetToken(String email, String token, Duration ttl) {
        log.info("Password reset for {} — token {} valid for {} minutes (mail not configured, logged only)",
                email, token, ttl.toMinutes());
    }
}
