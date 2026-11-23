package com.app.meridian.user.utils;

import com.app.meridian.exceptions.ValidationFailure;
import com.app.meridian.utils.ValidationUtils;

/**
 * One rule set for passwords, used wherever a password is set (reset today, user creation next), so
 * the API cannot be talked into a weaker password through a side door.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    private PasswordPolicy() {
    }

    public static void validate(String rawPassword) {
        ValidationUtils.requireNonBlank(rawPassword, "Password");
        if (rawPassword.length() < MIN_LENGTH) {
            throw new ValidationFailure("Password must be at least " + MIN_LENGTH + " characters");
        }
        boolean hasLetter = rawPassword.chars().anyMatch(Character::isLetter);
        boolean hasDigit = rawPassword.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new ValidationFailure("Password must contain at least one letter and one number");
        }
    }
}
