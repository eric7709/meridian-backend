package com.app.meridian.termReport.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.app.meridian.student.services.GetCurrentStudent;
import com.app.meridian.termReport.TermReport;

import lombok.RequiredArgsConstructor;

/** Resolves "me" to a student id, then delegates to the cached loader. */
@Service
@RequiredArgsConstructor
public class GetMyReports {

    private final LoadStudentReports loadStudentReports;
    private final GetCurrentStudent getCurrentStudent;

    public List<TermReport> get(Long studentId) {
        Long target = studentId == null ? getCurrentStudent.get().getId() : studentId;
        return loadStudentReports.load(target);
    }
}
