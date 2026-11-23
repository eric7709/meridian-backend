package com.app.meridian.score.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.exceptions.ForbiddenException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.student.Student;
import com.app.meridian.gradeScale.GradeScale;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetRepository;
import com.app.meridian.score.Score;
import com.app.meridian.score.ScoreEvents;
import com.app.meridian.score.ScoreRepository;
import com.app.meridian.score.dto.ScoreEntryRequestDTO;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
import com.app.meridian.score.utils.ScoreMapper;
import com.app.meridian.security.SecurityUtils;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;
import com.app.meridian.gradeScale.services.GradeScaleLookup;
import com.app.meridian.resultSheet.services.CreateResultSheet;
import com.app.meridian.resultSheet.services.ResultVisibilityGate;

/**
 * Saves one score: CA out of 40, exam out of 60, total = CA + exam, and the grade and remark are
 * looked up from the grade scale. Only draft or rejected sheets accept edits.
 */
@Service
@RequiredArgsConstructor
public class SaveScore {

    private static final double CA_MAX = 40.0;
    private static final double EXAM_MAX = 60.0;

    private final ResultSheetRepository resultSheetRepository;
    private final ScoreRepository scoreRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeScaleLookup gradeScaleLookup;
    private final ResultVisibilityGate visibilityGate;
    private final CreateResultSheet createResultSheet;
    private final EntityResolver entityResolver;
    private final ScoreMapper scoreMapper;
    private final ScoreEvents scoreEvents;

    @Transactional
    public Score save(Long sheetId, Long studentId, ScoreEntryRequestDTO request) {
        ValidationUtils.requireNonNull(sheetId, "Result sheet ID");
        ValidationUtils.requireNonNull(studentId, "Student ID");
        ValidationUtils.requireNonNull(request, "Request body");
        ResultSheet sheet = editableSheet(sheetId);
        Student student = entityResolver.resolveStudent(studentId);
        requireClassMembership(student, sheet);
        if (request.ca() != null) {
            ValidationUtils.requireInRange(request.ca(), 0, CA_MAX, "CA score");
        }
        if (request.exam() != null) {
            ValidationUtils.requireInRange(request.exam(), 0, EXAM_MAX, "Exam score");
        }
        if (request.ca() == null && request.exam() == null) {
            throw new BadRequestException("Provide a CA score, an exam score, or both");
        }
        Score score = scoreRepository.findBySheetIdAndStudentId(sheetId, studentId).orElseGet(() -> {
            Score created = new Score();
            created.setSheet(sheet);
            created.setStudent(student);
            return created;
        });
        double ca = request.ca() != null ? request.ca() : score.getCa() != null ? score.getCa() : 0.0;
        double exam = request.exam() != null ? request.exam() : score.getExam() != null ? score.getExam() : 0.0;
        double total = Math.round((ca + exam) * 100.0) / 100.0;
        score.setCa(ca);
        score.setExam(exam);
        score.setTotal(total);
        GradeScale grade = gradeScaleLookup.forTotal(total);
        score.setGrade(grade);
        if (grade != null) {
            score.setRemark(grade.getRemark());
        } else if (request.remark() != null && !request.remark().isBlank()) {
            score.setRemark(request.remark().trim());
        }
        Score saved = scoreRepository.save(score);
        scoreEvents.broadcastUpdated(scoreMapper.toResponse(saved));
        return saved;
    }

    /** The sheet must be editable, owned by the caller, in an open term. */
    private ResultSheet editableSheet(Long sheetId) {
        ResultSheet sheet = resultSheetRepository.findById(sheetId)
                .orElseThrow(() -> new NotFoundException("Result sheet not found"));
        if (!SecurityUtils.isAdmin()) {
            createResultSheet.requireOwner(sheet.getAssignment());
        }
        if (sheet.getStatus() != ResultSheetStatus.DRAFT && sheet.getStatus() != ResultSheetStatus.REJECTED) {
            throw new BadRequestException("This sheet is " + sheet.getStatus().name().toLowerCase()
                    + " and can no longer be edited");
        }
        visibilityGate.requireSubmittable(sheet.getTerm());
        return sheet;
    }

    private void requireClassMembership(Student student, ResultSheet sheet) {
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndSessionId(student.getId(), sheet.getTerm().getSession().getId())
                .orElseThrow(() -> new BadRequestException("This student is not enrolled for the session"));
        if (!enrollment.getClassRoom().getId().equals(sheet.getAssignment().getClassRoom().getId())) {
            throw new ForbiddenException("This student is not in the class this sheet belongs to");
        }
    }
}
