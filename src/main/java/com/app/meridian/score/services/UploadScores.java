package com.app.meridian.score.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetRepository;
import com.app.meridian.score.dto.ScoreEntryRequestDTO;
import com.app.meridian.resultSheet.dto.UploadScoresResultDTO;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Excel/CSV import: studentNo,ca,exam[,remark] — one line per student. */
@Service
@RequiredArgsConstructor
public class UploadScores {

    private final ResultSheetRepository resultSheetRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SaveScore saveScore;

    @Transactional
    public UploadScoresResultDTO upload(Long sheetId, MultipartFile file) {
        ValidationUtils.requireNonNull(sheetId, "Result sheet ID");
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Upload a CSV file with studentNo, ca and exam columns");
        }
        ResultSheet sheet = resultSheetRepository.findById(sheetId)
                .orElseThrow(() -> new NotFoundException("Result sheet not found"));
        List<Enrollment> roster = enrollmentRepository.findByClassRoomIdAndSessionId(
                sheet.getAssignment().getClassRoom().getId(), sheet.getTerm().getSession().getId());
        int saved = 0;
        List<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] cells = line.split(",");
                if (lineNumber == 1 && !cells[0].trim().matches("(?i)student_?no")) {
                    // header row that is not data: fall through and treat as data
                } else if (lineNumber == 1) {
                    continue;
                }
                if (cells.length < 3) {
                    errors.add("Line " + lineNumber + ": expected studentNo,ca,exam");
                    continue;
                }
                String studentNo = cells[0].trim();
                Enrollment match = roster.stream()
                        .filter(enrollment -> studentNo.equalsIgnoreCase(enrollment.getStudent().getStudentNo()))
                        .findFirst()
                        .orElse(null);
                if (match == null) {
                    errors.add("Line " + lineNumber + ": no enrolled student with number " + studentNo);
                    continue;
                }
                try {
                    Double ca = parse(cells[1]);
                    Double exam = parse(cells[2]);
                    String remark = cells.length > 3 ? cells[3].trim() : null;
                    saveScore.save(sheetId, match.getStudent().getId(), new ScoreEntryRequestDTO(ca, exam, remark));
                    saved++;
                } catch (RuntimeException ex) {
                    errors.add("Line " + lineNumber + ": " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new BadRequestException("Could not read the uploaded file");
        }
        return new UploadScoresResultDTO(saved, errors);
    }

    private Double parse(String cell) {
        String value = cell == null ? "" : cell.trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("'" + value + "' is not a number");
        }
    }
}
