package com.app.meridian.resultSheet.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.services.GetCurrentStaff;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetRepository;
import com.app.meridian.score.Score;
import com.app.meridian.score.ScoreRepository;
import com.app.meridian.resultSheet.dto.ScoreRosterDTO;
import com.app.meridian.resultSheet.dto.ScoreRosterRowDTO;
import com.app.meridian.security.SecurityUtils;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;
import com.app.meridian.exceptions.ForbiddenException;

/**
 * The result-entry table: every student enrolled in the sheet's class, whether they are marked,
 * plus the counts behind the tabs and the progress bar.
 */
@Service
@RequiredArgsConstructor
public class GetResultSheetScores {

    private final ResultSheetRepository resultSheetRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ScoreRepository scoreRepository;
    private final GetCurrentStaff getCurrentStaff;

    @Transactional(readOnly = true)
    public ScoreRosterDTO roster(Long sheetId, String search, String marked) {
        ValidationUtils.requireNonNull(sheetId, "Result sheet ID");
        ResultSheet sheet = resultSheetRepository.findById(sheetId)
                .orElseThrow(() -> new NotFoundException("Result sheet not found"));
        if (!SecurityUtils.isAdmin() && sheet.getAssignment().getTeacher() != null
                && !sheet.getAssignment().getTeacher().getId().equals(getCurrentStaff.get().getId())) {
            throw new com.app.meridian.exceptions.ForbiddenException(
                    "You can only view sheets for your own classes");
        }
        List<Enrollment> roster = enrollmentRepository.findByClassRoomIdAndSessionId(
                sheet.getAssignment().getClassRoom().getId(), sheet.getTerm().getSession().getId());
        Map<Long, Score> byStudent = scoreRepository.findBySheetId(sheetId).stream()
                .collect(Collectors.toMap(score -> score.getStudent().getId(), Function.identity(), (a, b) -> a));
        List<ScoreRosterRowDTO> all = roster.stream()
                .map(enrollment -> {
                    Score score = byStudent.get(enrollment.getStudent().getId());
                    boolean isMarked = score != null && score.getTotal() != null;
                    return new ScoreRosterRowDTO(enrollment.getStudent().getId(),
                            enrollment.getStudent().getStudentNo(),
                            enrollment.getStudent().getUser().getFullName(),
                            score == null ? null : score.getCa(),
                            score == null ? null : score.getExam(),
                            score == null ? null : score.getTotal(),
                            score == null || score.getGrade() == null ? null : score.getGrade().getGrade(),
                            score == null ? null : score.getRemark(),
                            isMarked);
                })
                .sorted(Comparator.comparing(ScoreRosterRowDTO::studentName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<ScoreRosterRowDTO> visible = all.stream().filter(row -> matches(row, search, marked)).toList();
        List<Double> totals = all.stream().filter(ScoreRosterRowDTO::marked).map(ScoreRosterRowDTO::total).toList();
        long markedCount = totals.size();
        Double average = totals.isEmpty() ? null
                : Math.round(totals.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 100.0) / 100.0;
        return new ScoreRosterDTO(sheet.getId(), sheet.getCode(),
                sheet.getAssignment().getSubject().getName(),
                sheet.getAssignment().getClassRoom().getName(),
                sheet.getTerm().getName().name(), sheet.getStatus().name(),
                all.size(), markedCount, all.size() - markedCount, average,
                totals.stream().max(Double::compareTo).orElse(null),
                totals.stream().min(Double::compareTo).orElse(null),
                new ArrayList<>(visible));
    }

    private boolean matches(ScoreRosterRowDTO row, String search, String marked) {
        if (search != null && !search.isBlank()) {
            String needle = search.trim().toLowerCase();
            String haystack = ((row.studentName() == null ? "" : row.studentName()) + " "
                    + (row.studentNo() == null ? "" : row.studentNo())).toLowerCase();
            if (!haystack.contains(needle)) {
                return false;
            }
        }
        if ("marked".equalsIgnoreCase(marked)) {
            return row.marked();
        }
        if ("notMarked".equalsIgnoreCase(marked) || "unmarked".equalsIgnoreCase(marked)) {
            return !row.marked();
        }
        return true;
    }
}
