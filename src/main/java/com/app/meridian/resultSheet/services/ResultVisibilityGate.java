package com.app.meridian.resultSheet.services;

import org.springframework.stereotype.Component;

import com.app.meridian.term.Term;
import com.app.meridian.exceptions.BadRequestException;

/**
 * The rules a sheet and its scores must satisfy before anyone touches them:
 * open term, correct status, and for students, the two visibility switches.
 */
@Component
public class ResultVisibilityGate {

    /** A locked term is read-only history; submission must also be switched on. */
    public void requireSubmittable(Term term) {
        if (Boolean.TRUE.equals(term.getIsLocked())) {
            throw new BadRequestException("This term is locked and can no longer be edited");
        }
        if (!Boolean.TRUE.equals(term.getAllowTeacherSubmission())) {
            throw new BadRequestException("Result submission is closed for this term");
        }
    }

    /** Students only ever see approved scores, when the school allows it. */
    public void requireStudentVisible(Term term) {
        if (!isStudentVisible(term)) {
            throw new BadRequestException("Results for this term have not been published yet");
        }
    }

    /** Same rule as a predicate, so a full-year view can skip unpublished terms. */
    public boolean isStudentVisible(Term term) {
        if (!Boolean.TRUE.equals(term.getAllowStudentView())) {
            return false;
        }
        return !Boolean.TRUE.equals(term.getIsLocked()) || Boolean.TRUE.equals(term.getIsActive());
    }
}
