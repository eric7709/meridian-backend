package com.app.meridian.resultSheet.dto;

/** A teacher opens a sheet for one teaching assignment in one term. */
public record ResultSheetDraftRequestDTO(Long assignmentId, Long termId) {
}
