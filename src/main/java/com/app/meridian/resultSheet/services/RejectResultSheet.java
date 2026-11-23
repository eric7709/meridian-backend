package com.app.meridian.resultSheet.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.auditLog.services.AuditTrail;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.services.GetCurrentStaff;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetEvents;
import com.app.meridian.resultSheet.ResultSheetRepository;
import com.app.meridian.resultSheet.dto.RejectSheetRequestDTO;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
import com.app.meridian.resultSheet.utils.ResultSheetMapper;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Rejection always carries a reason, and sends the sheet back to the teacher's queue. */
@Service
@RequiredArgsConstructor
public class RejectResultSheet {

    private final ResultSheetRepository resultSheetRepository;
    private final ResultSheetEvents resultSheetEvents;
    private final ResultSheetMapper resultSheetMapper;
    private final GetCurrentStaff getCurrentStaff;
    private final AuditTrail auditTrail;

    @Transactional
    public ResultSheet reject(Long id, RejectSheetRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Result sheet ID");
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.reason(), "Rejection reason");
        ResultSheet sheet = resultSheetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Result sheet not found"));
        if (sheet.getStatus() != ResultSheetStatus.PENDING) {
            throw new BadRequestException("Only a sheet that is pending review can be rejected");
        }
        sheet.setStatus(ResultSheetStatus.REJECTED);
        sheet.setReviewedBy(getCurrentStaff.get());
        sheet.setReviewedAt(LocalDateTime.now());
        sheet.setRejectionReason(request.reason().trim());
        ResultSheet saved = resultSheetRepository.save(sheet);
        resultSheetEvents.broadcastUpdated(resultSheetMapper.toResponse(saved));
        auditTrail.record("RESULT_SHEET_REJECTED", "ResultSheet", saved.getId(),
                "Rejected " + saved.getCode() + ": " + saved.getRejectionReason());
        return saved;
    }
}
