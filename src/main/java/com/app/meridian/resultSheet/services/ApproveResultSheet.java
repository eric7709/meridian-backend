package com.app.meridian.resultSheet.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.auditLog.services.AuditTrail;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.Staff;
import com.app.meridian.staff.services.GetCurrentStaff;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetEvents;
import com.app.meridian.resultSheet.ResultSheetRepository;
import com.app.meridian.resultSheet.dto.ResultSheetResponseDTO;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
import com.app.meridian.resultSheet.utils.ResultSheetMapper;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;
import com.app.meridian.termReport.services.ComputeTermReports;

/** Admin approval publishes the sheet and refreshes the term reports for that class. */
@Service
@RequiredArgsConstructor
public class ApproveResultSheet {

    private final ResultSheetRepository resultSheetRepository;
    private final ResultSheetEvents resultSheetEvents;
    private final ResultSheetMapper resultSheetMapper;
    private final GetCurrentStaff getCurrentStaff;
    private final ComputeTermReports computeTermReports;
    private final AuditTrail auditTrail;

    @Transactional
    public ResultSheet approve(Long id) {
        ValidationUtils.requireNonNull(id, "Result sheet ID");
        ResultSheet sheet = resultSheetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Result sheet not found"));
        if (sheet.getStatus() != ResultSheetStatus.PENDING) {
            throw new BadRequestException("Only a sheet that is pending review can be approved");
        }
        Staff reviewer = getCurrentStaff.get();
        sheet.setStatus(ResultSheetStatus.APPROVED);
        sheet.setReviewedBy(reviewer);
        sheet.setReviewedAt(LocalDateTime.now());
        sheet.setRejectionReason(null);
        ResultSheet saved = resultSheetRepository.save(sheet);
        computeTermReports.compute(saved.getTerm(), saved.getAssignment().getClassRoom().getId());
        ResultSheetResponseDTO payload = resultSheetMapper.toResponse(saved);
        resultSheetEvents.broadcastUpdated(payload);
        auditTrail.record("RESULT_SHEET_APPROVED", "ResultSheet", saved.getId(),
                "Approved " + saved.getCode());
        return saved;
    }
}
