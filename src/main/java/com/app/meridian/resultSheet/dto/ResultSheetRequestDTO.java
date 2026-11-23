package com.app.meridian.resultSheet.dto;

import java.time.LocalDateTime;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record ResultSheetRequestDTO(String code, ResultSheetStatus status, LocalDateTime submittedAt, LocalDateTime reviewedAt, String rejectionReason, Long assignmentId, Long termId, Long submittedById, Long reviewedById) {
}
