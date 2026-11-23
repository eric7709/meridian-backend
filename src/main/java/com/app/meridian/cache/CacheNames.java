package com.app.meridian.cache;

import java.util.List;

/**
 * Every cache the API uses. Keep this list and the TTL table in application.yml in step: a name
 * that is missing here silently falls back to the default TTL.
 */
public final class CacheNames {

    /** Grade lookup by total score — read on every single score save. */
    public static final String GRADE_SCALE_BY_TOTAL = "gradeScaleByTotal";

    /** The single settings row; gates the optional modules. */
    public static final String SCHOOL_SETTINGS = "schoolSettings";

    /** The active term, read by every teacher/student screen. */
    public static final String ACTIVE_TERM = "activeTerm";

    /** Role-filtered announcement feed for the dashboards. */
    public static final String ANNOUNCEMENT_FEED = "announcementFeed";

    /** One class's timetable for a term. */
    public static final String CLASS_TIMETABLE = "classTimetable";

    /** A student's term reports (average, position, form teacher remark). */
    public static final String TERM_REPORTS_BY_STUDENT = "termReportsByStudent";

    public static final List<String> ALL = List.of(GRADE_SCALE_BY_TOTAL, SCHOOL_SETTINGS, ACTIVE_TERM,
            ANNOUNCEMENT_FEED, CLASS_TIMETABLE, TERM_REPORTS_BY_STUDENT);

    private CacheNames() {
    }
}
