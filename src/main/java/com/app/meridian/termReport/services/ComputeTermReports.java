package com.app.meridian.termReport.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.term.Term;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.student.Student;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetRepository;
import com.app.meridian.score.Score;
import com.app.meridian.score.ScoreRepository;
import com.app.meridian.termReport.TermReport;
import com.app.meridian.cache.CacheNames;
import com.app.meridian.termReport.TermReportRepository;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/**
 * Averages and class positions are recomputed from approved sheets only, every time a sheet is
 * approved. Ties share a position.
 */
@Service
@RequiredArgsConstructor
public class ComputeTermReports {

    private final ResultSheetRepository resultSheetRepository;
    private final ScoreRepository scoreRepository;
    private final TermReportRepository termReportRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    @CacheEvict(cacheNames = CacheNames.TERM_REPORTS_BY_STUDENT, allEntries = true)
    public int compute(Term term, Long classId) {
        ValidationUtils.requireNonNull(term, "Term");
        ValidationUtils.requireNonNull(classId, "Class");
        List<Enrollment> roster = enrollmentRepository.findByClassRoomIdAndSessionId(classId,
                term.getSession().getId());
        if (roster.isEmpty()) {
            throw new BadRequestException("This class has no students enrolled for the session");
        }
        List<ResultSheet> approved = resultSheetRepository
                .findByAssignmentClassRoomIdAndTermIdAndStatus(classId, term.getId(), ResultSheetStatus.APPROVED);
        List<Score> scores = approved.isEmpty() ? List.of()
                : scoreRepository.findBySheetIdIn(approved.stream().map(ResultSheet::getId).toList());
        Map<Long, Double> averages = new HashMap<>();
        Map<Long, Student> students = new HashMap<>();
        for (Enrollment enrollment : roster) {
            students.put(enrollment.getStudent().getId(), enrollment.getStudent());
        }
        for (Score score : scores) {
            if (score.getTotal() == null || !students.containsKey(score.getStudent().getId())) {
                continue;
            }
            averages.merge(score.getStudent().getId(), score.getTotal(), Double::sum);
        }
        Map<Long, Integer> subjectCounts = new HashMap<>();
        for (Score score : scores) {
            if (score.getTotal() != null && students.containsKey(score.getStudent().getId())) {
                subjectCounts.merge(score.getStudent().getId(), 1, Integer::sum);
            }
        }
        averages.replaceAll((studentId, sum) -> round(sum / subjectCounts.get(studentId)));
        List<Map.Entry<Long, Double>> ranked = new ArrayList<>(averages.entrySet());
        ranked.sort(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()));
        Map<Long, Integer> positions = new HashMap<>();
        int lastPosition = 0;
        Double previous = null;
        for (int i = 0; i < ranked.size(); i++) {
            Map.Entry<Long, Double> entry = ranked.get(i);
            if (previous == null || Double.compare(previous, entry.getValue()) != 0) {
                lastPosition = i + 1;
                previous = entry.getValue();
            }
            positions.put(entry.getKey(), lastPosition);
        }
        for (Map.Entry<Long, Double> entry : ranked) {
            Long studentId = entry.getKey();
            TermReport report = termReportRepository
                    .findByStudentIdAndTermId(studentId, term.getId())
                    .orElseGet(() -> {
                        TermReport created = new TermReport();
                        created.setStudent(students.get(studentId));
                        created.setTerm(term);
                        return created;
                    });
            report.setAverage(entry.getValue());
            report.setClassPosition(positions.get(studentId));
            report.setClassSize(roster.size());
            termReportRepository.save(report);
        }
        return ranked.size();
    }

    private Double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
