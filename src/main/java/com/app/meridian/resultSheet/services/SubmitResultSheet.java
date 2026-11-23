package com.app.meridian.resultSheet.services;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.term.Term;
import com.app.meridian.auditLog.services.AuditTrail;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.Staff;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetEvents;
import com.app.meridian.resultSheet.ResultSheetRepository;
import com.app.meridian.score.Score;
import com.app.meridian.score.ScoreRepository;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
import com.app.meridian.resultSheet.utils.ResultSheetMapper;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Submitting is only allowed once every enrolled student has a total. */
@Service
@RequiredArgsConstructor
public class SubmitResultSheet {

    private final ResultSheetRepository resultSheetRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ScoreRepository scoreRepository;
    private final ResultSheetEvents resultSheetEvents;
    private final ResultSheetMapper resultSheetMapper;
    private final ResultVisibilityGate visibilityGate;
    private final CreateResultSheet createResultSheet;
    private final AuditTrail auditTrail;

    @Transactional
    public ResultSheet submit(Long id) {
        ValidationUtils.requireNonNull(id, "Result sheet ID");
        ResultSheet sheet = resultSheetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Result sheet not found"));
        createResultSheet.requireOwner(sheet.getAssignment());
        Term term = sheet.getTerm();
        visibilityGate.requireSubmittable(term);
        if (sheet.getStatus() != ResultSheetStatus.DRAFT && sheet.getStatus() != ResultSheetStatus.REJECTED) {
            throw new BadRequestException("Only a draft or rejected sheet can be submitted");
        }
        List<Enrollment> roster = enrollmentRepository.findByClassRoomIdAndSessionId(
                sheet.getAssignment().getClassRoom().getId(), term.getSession().getId());
        if (roster.isEmpty()) {
            throw new BadRequestException("This class has no students enrolled for the session");
        }
        Set<Long> marked = scoreRepository.findBySheetId(id).stream()
                .filter(score -> score.getTotal() != null)
                .map(score -> score.getStudent().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<String> missing = roster.stream()
                .filter(enrollment -> !marked.contains(enrollment.getStudent().getId()))
                .map(enrollment -> enrollment.getStudent().getUser().getFullName())
                .toList();
        if (!missing.isEmpty()) {
            String sample = missing.stream().limit(5).collect(Collectors.joining(", "));
            throw new BadRequestException("Scores are still missing for " + missing.size()
                    + " student(s): " + sample + (missing.size() > 5 ? "…" : ""));
        }
        Staff me = sheet.getSubmittedBy();
        sheet.setStatus(ResultSheetStatus.PENDING);
        sheet.setSubmittedAt(LocalDateTime.now());
        sheet.setRejectionReason(null);
        if (me == null) {
            sheet.setSubmittedBy(sheet.getAssignment().getTeacher());
        }
        ResultSheet saved = resultSheetRepository.save(sheet);
        resultSheetEvents.broadcastUpdated(resultSheetMapper.toResponse(saved));
        auditTrail.record("RESULT_SHEET_SUBMITTED", "ResultSheet", saved.getId(),
                "Submitted " + saved.getCode() + " for review");
        return saved;
    }
}
