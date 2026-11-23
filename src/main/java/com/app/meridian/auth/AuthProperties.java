package com.app.meridian.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param exposeResetToken return the password-reset token in the response instead of only mailing it.
 *                         Convenient while there is no SMTP server wired into {@code ResetMailer}.
 */
@ConfigurationProperties(prefix = "meridian.auth")
public record AuthProperties(boolean exposeResetToken) {
}
