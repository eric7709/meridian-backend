package com.app.meridian.student.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.academicSession.AcademicSessionRepository;
import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.term.Term;
import com.app.meridian.term.TermRepository;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.exceptions.ForbiddenException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.student.Student;
import com.app.meridian.student.services.GetCurrentStudent;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetRepository;
import com.app.meridian.score.Score;
import com.app.meridian.score.ScoreRepository;
import com.app.meridian.termReport.TermReport;
import com.app.meridian.termReport.TermReportRepository;
import com.app.meridian.student.dto.StudentResultsDTO;
import com.app.meridian.student.dto.StudentSubjectScoreDTO;
import com.app.meridian.student.dto.StudentTermResultDTO;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
import com.app.meridian.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import com.app.meridian.resultSheet.services.ResultVisibilityGate;
import com.app.meridian.student.StudentRepository;

/**
 * A student's own results: approved sheets only, gated by allowStudentView, with the "Full year"
 * view being the average across the session's terms.
 */
@Service
@RequiredArgsConstructor
public class GetStudentResults {

    private final GetCurrentStudent getCurrentStudent;
    private final com.app.meridian.student.StudentRepository studentRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final TermRepository termRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ResultSheetRepository resultSheetRepository;
    private final ScoreRepository scoreRepository;
    private final TermReportRepository termReportRepository;
    private final ResultVisibilityGate visibilityGate;

    @Transactional(readOnly = true)
    public StudentResultsDTO get(Long studentId, Long sessionId, String term) {
        Student student = resolve(studentId);
        boolean ownRecord = SecurityUtils.principal().isStudent();
        AcademicSession session = sessionId == null
                ? academicSessionRepository.findByIsCurrentTrue()
                        .orElseThrow(() -> new BadRequestException("No current session is set"))
                : academicSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new NotFoundException("Session not found"));
        List<Term> terms = termRepository.findBySessionId(session.getId());
        boolean fullYear = term == null || term.isBlank() || term.toLowerCase().startsWith("full");
        List<Term> scope = fullYear ? terms : terms.stream()
                .filter(t -> t.getName().name().equalsIgnoreCase(term.trim()))
                .toList();
        if (scope.isEmpty()) {
            throw new BadRequestException("That term does not belong to the selected session");
        }
        List<StudentTermResultDTO> results = new ArrayList<>();
        List<Double> averages = new ArrayList<>();
        for (Term t : scope) {
            if (ownRecord) {
                if (fullYear) {
                    // A full-year average simply ignores terms that are not published yet.
                    if (!visibilityGate.isStudentVisible(t)) {
                        continue;
                    }
                } else {
                    visibilityGate.requireStudentVisible(t);
                }
            }
            StudentTermResultDTO termResult = termResult(student, session, t);
            if (termResult != null) {
                results.add(termResult);
                if (termResult.average() != null) {
                    averages.add(termResult.average());
                }
            }
        }
        Double overall = averages.isEmpty() ? null
                : Math.round(averages.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 100.0) / 100.0;
        return new StudentResultsDTO(student.getStudentNo(), student.getUser().getFullName(),
                session.getLabel(), fullYear ? "FULL_YEAR" : scope.get(0).getName().name(), overall, results);
    }

    private StudentTermResultDTO termResult(Student student, AcademicSession session, Term term) {
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndSessionId(student.getId(), session.getId()).orElse(null);
        if (enrollment == null) {
            return null;
        }
        List<ResultSheet> sheets = resultSheetRepository.findByAssignmentClassRoomIdAndTermIdAndStatus(
                enrollment.getClassRoom().getId(), term.getId(), ResultSheetStatus.APPROVED);
        if (sheets.isEmpty()) {
            return null;
        }
        List<Score> allScores = scoreRepository.findBySheetIdIn(sheets.stream().map(ResultSheet::getId).toList());
        Map<Long, List<Score>> bySheet = allScores.stream()
                .collect(Collectors.groupingBy(score -> score.getSheet().getId()));
        List<StudentSubjectScoreDTO> subjects = new ArrayList<>();
        for (ResultSheet sheet : sheets) {
            List<Score> sheetScores = bySheet.getOrDefault(sheet.getId(), List.of()).stream()
                    .filter(score -> score.getTotal() != null)
                    .toList();
            Score mine = sheetScores.stream()
                    .filter(score -> score.getStudent().getId().equals(student.getId()))
                    .findFirst().orElse(null);
            if (mine == null) {
                continue;
            }
            Double classAverage = sheetScores.isEmpty() ? null
                    : Math.round(sheetScores.stream().mapToDouble(Score::getTotal).average().orElse(0) * 100.0) / 100.0;
            List<Score> ranked = sheetScores.stream()
                    .sorted(Comparator.comparing(Score::getTotal).reversed())
                    .toList();
            int position = ranked.indexOf(mine) + 1;
            subjects.add(new StudentSubjectScoreDTO(
                    sheet.getAssignment().getSubject().getName(),
                    sheet.getAssignment().getSubject().getCode(),
                    mine.getCa(), mine.getExam(), mine.getTotal(),
                    mine.getGrade() == null ? null : mine.getGrade().getGrade(),
                    mine.getRemark(), classAverage, position));
        }
        if (subjects.isEmpty()) {
            return null;
        }
        subjects.sort(Comparator.comparing(StudentSubjectScoreDTO::subjectName, String.CASE_INSENSITIVE_ORDER));
        TermReport report = termReportRepository.findByStudentIdAndTermId(student.getId(), term.getId())
                .orElse(null);
        Double average = report != null ? report.getAverage()
                : Math.round(subjects.stream().filter(s -> s.total() != null)
                        .mapToDouble(StudentSubjectScoreDTO::total).average().orElse(0) * 100.0) / 100.0;
        return new StudentTermResultDTO(term.getName().name(), average,
                report == null ? null : report.getClassPosition(),
                report == null ? null : report.getClassSize(),
                report == null ? null : report.getFormTeacherRemark(),
                subjects);
    }

    /**
     * Students may only read their own record; teachers and admins may read any student they can
     * already see in the directory.
     */
    private Student resolve(Long studentId) {
        if (SecurityUtils.principal().isStudent()) {
            Long own = getCurrentStudent.get().getId();
            if (studentId != null && !own.equals(studentId)) {
                throw new ForbiddenException("You can only read your own results");
            }
            return getCurrentStudent.get();
        }
        if (studentId == null) {
            throw new BadRequestException("Specify which student's results you want");
        }
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student " + studentId + " not found"));
    }
}
