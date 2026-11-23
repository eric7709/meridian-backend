package com.app.meridian.enrollment.dto;

import java.util.List;

/** End-of-session promotion: move students from one class/session into another. */
public record PromoteRequestDTO(Long fromSessionId, Long toSessionId, Long fromClassId, Long toClassId,
        List<Long> studentIds) {
}
