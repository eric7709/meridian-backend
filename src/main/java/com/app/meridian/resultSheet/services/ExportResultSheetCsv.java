package com.app.meridian.resultSheet.services;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.resultSheet.dto.ScoreRosterDTO;
import com.app.meridian.resultSheet.dto.ScoreRosterRowDTO;

import lombok.RequiredArgsConstructor;
import com.app.meridian.student.Student;

/** Master sheet export: the same roster the teacher sees, as CSV. */
@Service
@RequiredArgsConstructor
public class ExportResultSheetCsv {

    private final GetResultSheetScores getResultSheetScores;

    @Transactional(readOnly = true)
    public byte[] export(Long sheetId) {
        ScoreRosterDTO roster = getResultSheetScores.roster(sheetId, null, null);
        StringBuilder csv = new StringBuilder();
        csv.append("Student No,Student Name,CA (40),Exam (60),Total,Grade,Remark\n");
        for (ScoreRosterRowDTO row : roster.rows()) {
            csv.append(quote(row.studentNo())).append(',')
                    .append(quote(row.studentName())).append(',')
                    .append(value(row.ca())).append(',')
                    .append(value(row.exam())).append(',')
                    .append(value(row.total())).append(',')
                    .append(quote(row.grade())).append(',')
                    .append(quote(row.remark())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String value(Double number) {
        return number == null ? "" : String.valueOf(number);
    }

    private String quote(String text) {
        if (text == null) {
            return "";
        }
        String escaped = text.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
