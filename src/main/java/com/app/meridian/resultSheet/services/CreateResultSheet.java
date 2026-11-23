package com.app.meridian.resultSheet.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.teachingAssignment.TeachingAssignment;
import com.app.meridian.term.Term;
import com.app.meridian.auditLog.services.AuditTrail;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.ForbiddenException;
import com.app.meridian.staff.Staff;
import com.app.meridian.staff.services.GetCurrentStaff;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetEvents;
import com.app.meridian.resultSheet.ResultSheetRepository;
import com.app.meridian.resultSheet.dto.ResultSheetDraftRequestDTO;
import com.app.meridian.resultSheet.dto.ResultSheetResponseDTO;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
import com.app.meridian.resultSheet.utils.ResultSheetMapper;
import com.app.meridian.security.SecurityUtils;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Teachers open a draft sheet for one assignment and term; the code is generated. */
@Service
@RequiredArgsConstructor
public class CreateResultSheet {

    private final EntityResolver entityResolver;
    private final ResultSheetRepository resultSheetRepository;
    private final ResultSheetEvents resultSheetEvents;
    private final ResultSheetMapper resultSheetMapper;
    private final ResultVisibilityGate visibilityGate;
    private final GetCurrentStaff getCurrentStaff;
    private final AuditTrail auditTrail;

    @Transactional
    public ResultSheet create(ResultSheetDraftRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonNull(request.assignmentId(), "Teaching assignment");
        ValidationUtils.requireNonNull(request.termId(), "Term");
        TeachingAssignment assignment = entityResolver.resolveTeachingAssignment(request.assignmentId());
        Term term = entityResolver.resolveTerm(request.termId());
        visibilityGate.requireSubmittable(term);
        Staff owner = requireOwner(assignment);
        if (resultSheetRepository.existsByAssignmentIdAndTermId(assignment.getId(), term.getId())) {
            throw new ConflictException("A result sheet already exists for this subject, class and term");
        }
        ResultSheet sheet = new ResultSheet();
        sheet.setCode("RES-" + String.format("%03d", resultSheetRepository.count() + 1));
        sheet.setAssignment(assignment);
        sheet.setTerm(term);
        sheet.setStatus(ResultSheetStatus.DRAFT);
        sheet.setSubmittedBy(owner);
        ResultSheet saved = resultSheetRepository.save(sheet);
        ResultSheetResponseDTO payload = resultSheetMapper.toResponse(saved);
        resultSheetEvents.broadcastCreated(payload);
        auditTrail.record("RESULT_SHEET_CREATED", "ResultSheet", saved.getId(),
                "Opened " + assignment.getSubject().getName() + " for " + assignment.getClassRoom().getName());
        return saved;
    }

    /** A teacher may only touch sheets for assignments they own. Admins may touch all. */
    public Staff requireOwner(TeachingAssignment assignment) {
        if (SecurityUtils.isAdmin()) {
            return getCurrentStaff.get();
        }
        Staff me = getCurrentStaff.get();
        if (assignment.getTeacher() == null || !assignment.getTeacher().getId().equals(me.getId())) {
            throw new ForbiddenException("You can only work on classes you are assigned to teach");
        }
        return me;
    }
}
