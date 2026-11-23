package com.app.meridian.utils;

import java.util.Collection;

import com.app.meridian.exceptions.BadRequestException;

/** Small guard clauses so services read as rules instead of nested if-statements. */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void requireNonNull(Object value, String field) {
        if (value == null) {
            throw new BadRequestException(field + " is required");
        }
    }

    public static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(field + " is required");
        }
    }

    public static void requireNotEmpty(Collection<?> value, String field) {
        if (value == null || value.isEmpty()) {
            throw new BadRequestException(field + " must not be empty");
        }
    }

    public static void requireGreaterThanZero(Double value, String field) {
        requireNonNull(value, field);
        if (value <= 0) {
            throw new BadRequestException(field + " must be greater than zero");
        }
    }

    public static void requireInRange(Double value, double min, double max, String field) {
        requireNonNull(value, field);
        if (value < min || value > max) {
            throw new BadRequestException(field + " must be between " + (int) min + " and " + (int) max);
        }
    }

    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new BadRequestException(message);
        }
    }

    public static String trimOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
