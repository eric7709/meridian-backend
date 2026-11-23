package com.app.meridian.score.dto;

/** CA is out of 40, exam out of 60; the total and grade are computed server-side. */
public record ScoreEntryRequestDTO(Double ca, Double exam, String remark) {
}
