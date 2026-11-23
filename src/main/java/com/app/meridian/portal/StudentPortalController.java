package com.app.meridian.portal;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.enrollment.dto.EnrollmentResponseDTO;
import com.app.meridian.enrollment.services.GetStudentEnrollments;
import com.app.meridian.enrollment.utils.EnrollmentMapper;
import com.app.meridian.feePayment.dto.FeePaymentResponseDTO;
import com.app.meridian.feePayment.services.GetStudentFees;
import com.app.meridian.feePayment.utils.FeePaymentMapper;
import com.app.meridian.attendance.dto.AttendanceSummaryDTO;
import com.app.meridian.attendance.services.GetAttendanceSummary;
import com.app.meridian.student.dto.StudentResponseDTO;
import com.app.meridian.student.services.GetCurrentStudent;
import com.app.meridian.student.utils.StudentMapper;
import com.app.meridian.student.dto.StudentResultsDTO;
import com.app.meridian.student.dto.SubjectHistoryRowDTO;
import com.app.meridian.termReport.dto.TermReportResponseDTO;
import com.app.meridian.termReport.services.GetMyReports;
import com.app.meridian.student.services.GetStudentResults;
import com.app.meridian.student.services.GetStudentSubjectHistory;
import com.app.meridian.termReport.services.RenderReportCard;
import com.app.meridian.termReport.utils.TermReportMapper;

import lombok.RequiredArgsConstructor;

/** The student's own read-only view. Every "me" endpoint resolves the student from the token. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/students")
public class StudentPortalController {

    private final GetCurrentStudent getCurrentStudent;
    private final StudentMapper studentMapper;
    private final GetStudentResults getStudentResults;
    private final GetStudentSubjectHistory getStudentSubjectHistory;
    private final GetMyReports getMyReports;
    private final GetStudentEnrollments getStudentEnrollments;
    private final GetAttendanceSummary getAttendanceSummary;
    private final GetStudentFees getStudentFees;
    private final RenderReportCard renderReportCard;
    private final TermReportMapper termReportMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final FeePaymentMapper feePaymentMapper;

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentResponseDTO me() {
        return studentMapper.toResponse(getCurrentStudent.get());
    }

    @GetMapping("/me/results")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentResultsDTO results(@RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) String term) {
        return getStudentResults.get(null, sessionId, term);
    }

    @GetMapping("/me/results/subject/{subjectId}")
    @PreAuthorize("hasRole('STUDENT')")
    public List<SubjectHistoryRowDTO> subjectHistory(@PathVariable Long subjectId) {
        return getStudentSubjectHistory.get(null, subjectId);
    }

    @GetMapping("/me/results/history")
    @PreAuthorize("hasRole('STUDENT') or hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
    public List<SubjectHistoryRowDTO> history() {
        return getStudentSubjectHistory.get(null, null);
    }

    @GetMapping("/{studentId}/results/history")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
    public StudentResultsDTO historyFor(@PathVariable Long studentId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) String term) {
        return getStudentResults.get(studentId, sessionId, term);
    }

    @GetMapping("/me/reports")
    @PreAuthorize("hasRole('STUDENT')")
    public List<TermReportResponseDTO> reports() {
        return getMyReports.get(null).stream().map(termReportMapper::toResponse).toList();
    }

    @GetMapping("/me/report-card.pdf")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<byte[]> reportCard(@RequestParam Long termId) {
        byte[] pdf = renderReportCard.render(null, termId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-card.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/me/attendance/summary")
    @PreAuthorize("hasRole('STUDENT')")
    public AttendanceSummaryDTO attendance(@RequestParam(required = false) Long termId) {
        return getAttendanceSummary.forCurrentStudent(termId);
    }

    @GetMapping("/me/enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public List<EnrollmentResponseDTO> enrollments() {
        return getStudentEnrollments.forStudent(null).stream().map(enrollmentMapper::toResponse).toList();
    }

    @GetMapping("/me/fees")
    @PreAuthorize("hasRole('STUDENT')")
    public List<FeePaymentResponseDTO> fees() {
        return getStudentFees.forStudent(null).stream().map(feePaymentMapper::toResponse).toList();
    }
}
