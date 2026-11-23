package com.app.meridian.student.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.exceptions.ForbiddenException;
import com.app.meridian.student.services.GetCurrentStudent;
import com.app.meridian.score.Score;
import com.app.meridian.score.ScoreRepository;
import com.app.meridian.student.dto.SubjectHistoryRowDTO;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
import com.app.meridian.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import com.app.meridian.resultSheet.services.ResultVisibilityGate;

/** One subject, term by term: the "history" table on the student subject page. */
@Service
@RequiredArgsConstructor
public class GetStudentSubjectHistory {

    private final ScoreRepository scoreRepository;
    private final GetCurrentStudent currentStudent;
    private final ResultVisibilityGate visibilityGate;

    @Transactional(readOnly = true)
    public List<SubjectHistoryRowDTO> get(Long studentId, Long subjectId) {
        boolean student = SecurityUtils.principal().isStudent();
        Long own = student ? currentStudent.get().getId() : null;
        Long target = studentId != null ? studentId : own;
        if (target == null) {
            throw new ForbiddenException("Specify which student's history you want");
        }
        if (student && !target.equals(own)) {
            throw new ForbiddenException("You can only read your own results");
        }
        boolean ownRecord = student;
        return scoreRepository.findByStudentId(target).stream()
                .filter(score -> score.getSheet().getStatus() == ResultSheetStatus.APPROVED)
                .filter(score -> subjectId == null
                        || score.getSheet().getAssignment().getSubject().getId().equals(subjectId))
                .filter(score -> {
                    if (ownRecord) {
                        visibilityGate.requireStudentVisible(score.getSheet().getTerm());
                    }
                    return true;
                })
                .sorted(Comparator.comparing((Score score) -> score.getSheet().getTerm().getSession().getLabel())
                        .thenComparing(score -> score.getSheet().getTerm().getName()))
                .map(score -> new SubjectHistoryRowDTO(
                        score.getSheet().getTerm().getSession().getLabel(),
                        score.getSheet().getTerm().getName().name(),
                        score.getCa(), score.getExam(), score.getTotal(),
                        score.getGrade() == null ? null : score.getGrade().getGrade(),
                        score.getRemark()))
                .toList();
    }
}
