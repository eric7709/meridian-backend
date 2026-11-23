package com.app.meridian.resultSheet.dto;

import java.util.List;

/** The result-entry table plus the counts behind "All 38 / Not marked 33 / Marked 5". */
public record ScoreRosterDTO(Long sheetId, String code, String subjectName, String className,
        String termName, String status, long total, long marked, long notMarked,
        Double average, Double highest, Double lowest, List<ScoreRosterRowDTO> rows) {
}
